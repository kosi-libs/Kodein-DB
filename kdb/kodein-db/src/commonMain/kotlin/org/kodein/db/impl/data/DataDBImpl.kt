package org.kodein.db.impl.data

import org.kodein.db.*
import org.kodein.db.data.DataBatch
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataIndexMap
import org.kodein.db.data.DataSnapshot
import org.kodein.db.impl.utils.newLock
import org.kodein.db.impl.utils.withLock
import org.kodein.db.kv.KeyValueBatch
import org.kodein.db.kv.KeyValueDB
import org.kodein.db.kv.KeyValueSnapshot
import org.kodein.memory.io.*
import org.kodein.memory.use
import org.kodein.memory.util.deferScope
import org.kodein.memory.util.forEachResilient

internal class DataDBImpl(override val kv: KeyValueDB) : DataReadModule, DataDB {

    internal val lock = newLock()

    companion object {
        internal const val DEFAULT_CAPACITY = 8 * 1024
    }

    override fun currentOrNewSnapshot(): Pair<KeyValueSnapshot, Boolean> = kv.newSnapshot() to true

    internal fun deleteIndexesInBatch(batch: KeyValueBatch, documentKey: ReadMemory) {
        deferScope {
            val refKey = Allocation.native(documentKey.size) { writeRefKeyFromDocumentKey(documentKey) } .useInScope()
            val refBody = kv.get(refKey)?.useInScope() ?: return
            getRefIndexKeys(refKey, refBody).forEach { indexKey ->
                batch.delete(indexKey)
            }
            batch.delete(refKey)
        }
    }

    internal fun putIndexesInBatch(batch: KeyValueBatch, documentKey: ReadMemory, refKey: ReadMemory, indexes: DataIndexMap) {
        if (indexes.isEmpty())
            return

        val type = getDocumentKeyType(documentKey)
        val id = getDocumentKeyID(documentKey)

        ExpandableAllocation.native(DEFAULT_CAPACITY).use { bodyAllocation ->
            for ((name, data) in indexes) {
                for ((value, associatedData) in data) {
                    val indexBody = bodyAllocation.slice { writeIndexBody(id, value, associatedData) }
                    Allocation.native(getIndexKeySize(id, name, value)) { writeIndexKey(type, id, name, value) } .use { indexKey ->
                        batch.put(indexKey, indexBody)
                    }
                }
            }
        }

        Allocation.native(getRefBodySize(indexes)) { writeRefBody(indexes) } .use { refBody ->
            batch.put(refKey, refBody)
        }
    }

    private fun putInBatch(batch: KeyValueBatch, documentKey: ReadMemory, body: Body, indexes: DataIndexMap): Int {
        Allocation.native(documentKey.size) { writeRefKeyFromDocumentKey(documentKey) } .use { refKey ->
            deleteIndexesInBatch(batch, refKey)
            putIndexesInBatch(batch, documentKey, refKey, indexes)
        }

        ExpandableAllocation.native(DEFAULT_CAPACITY) { writeBody(body) } .use { bodyMemory ->
            batch.put(documentKey, bodyMemory)
            return bodyMemory.size
        }
    }

    @Suppress("DuplicatedCode")
    override fun put(key: ReadMemory, body: Body, indexes: DataIndexMap, vararg options: Options.DirectPut): Int {
        key.verifyDocumentKey()

        val anticipations = options.all<Anticipate>()
        val inLockAnticipations = options.all<AnticipateInLock>()
        val inLockReactions = options.all<ReactInLock>()
        val reactions = options.all<React>()

        anticipations.forEach { it.block() }
        val length = kv.newBatch().use { batch ->
            lock.withLock {
                inLockAnticipations.forEach { it.block(batch) }
                val length = putInBatch(batch, key, body, indexes)
                batch.write(*options.filterIsInstance<Options.BatchWrite>().toTypedArray())
                inLockReactions.forEachResilient { it.block(length) }
                length
            }
        }
        reactions.forEachResilient { it.block(length) }
        return length
    }

    private fun deleteInBatch(batch: KeyValueBatch, documentKey: ReadMemory) {
        Allocation.native(documentKey.size) { writeRefKeyFromDocumentKey(documentKey) } .use { refKey ->
            deleteIndexesInBatch(batch, refKey)
        }

        batch.delete(documentKey)
    }

    @Suppress("DuplicatedCode")
    override fun delete(key: ReadMemory, vararg options: Options.DirectDelete) {
        key.verifyDocumentKey()
        val anticipations = options.all<Anticipate>()
        val inLockAnticipations = options.all<AnticipateInLock>()
        val inLockReactions = options.all<ReactInLock>()
        val reactions = options.all<React>()

        anticipations.forEach { it.block() }
        kv.newBatch().use { batch ->
            lock.withLock {
                inLockAnticipations.forEach { it.block(batch) }
                deleteInBatch(batch, key)
                batch.write(*options.filterIsInstance<Options.BatchWrite>().toTypedArray())
                inLockReactions.forEachResilient { it.block(-1) }
            }
        }
        reactions.forEachResilient { it.block(-1) }
    }

    override fun newBatch(vararg options: Options.NewBatch): DataBatch = DataBatchImpl(this, kv.newBatch(*options))

    override fun newSnapshot(vararg options: Options.NewSnapshot): DataSnapshot = DataSnapshotImpl(kv.newSnapshot(*options))

    override fun <T : Any> getExtension(key: ExtensionKey<T>): T? = null

    override fun close() {
        kv.close()
    }
}
