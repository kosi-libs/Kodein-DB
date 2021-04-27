package org.kodein.db.impl.data

import org.kodein.db.*
import org.kodein.db.data.DataBatch
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataIndexMap
import org.kodein.db.data.DataSnapshot
import org.kodein.db.impl.utils.newLock
import org.kodein.db.impl.utils.withLock
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.*
import org.kodein.memory.text.toHex
import org.kodein.memory.use
import org.kodein.memory.util.deferScope
import org.kodein.memory.util.forEachResilient

internal class DataDBImpl(override val ldb: LevelDB) : DataReadModule, DataDB {

    override val snapshot: LevelDB.Snapshot? get() = null

    internal val lock = newLock()

    companion object {
        internal const val DEFAULT_CAPACITY = 8 * 1024
    }

    internal fun deleteIndexesInBatch(batch: LevelDB.WriteBatch, documentKey: ReadMemory) {
        deferScope {
            val refKey = Allocation.native(documentKey.size) { writeRefKeyFromDocumentKey(documentKey) } .useInScope()
            val refBody = ldb.get(refKey)?.useInScope() ?: return
            getRefIndexKeys(refKey, refBody).forEach { indexKey ->
                batch.delete(indexKey)
            }
            batch.delete(refKey)
        }
    }

    internal fun putIndexesInBatch(batch: LevelDB.WriteBatch, documentKey: ReadMemory, refKey: ReadMemory, indexes: DataIndexMap) {
        if (indexes.isEmpty())
            return

        val type = getDocumentKeyType(documentKey)
        val id = getDocumentKeyID(documentKey)

        ExpandableAllocation.native(DEFAULT_CAPACITY).use { bodyAllocation ->
            for ((name, data) in indexes) {
                for ((value, metadata) in data) {
                    val indexBody = bodyAllocation.slice { writeIndexBody(id, value, metadata) }
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

    private fun putInBatch(batch: LevelDB.WriteBatch, documentKey: ReadMemory, body: Body, indexes: DataIndexMap): Int {
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
    override fun put(key: ReadMemory, body: Body, indexes: DataIndexMap, vararg options: Options.Write): Int {
        key.verifyDocumentKey()

        val anticipations = options.all<Anticipate>()
        val inLockAnticipations = options.all<AnticipateInLock>()
        val inLockReactions = options.all<ReactInLock>()
        val reactions = options.all<React>()

        anticipations.forEach { it.block() }
        val length = ldb.newWriteBatch().use { batch ->
            lock.withLock {
                inLockAnticipations.forEach { it.block(batch) }
                val length = putInBatch(batch, key, body, indexes)
                ldb.write(batch, LevelDB.WriteOptions.from(options))
                inLockReactions.forEachResilient { it.block(length) }
                length
            }
        }
        reactions.forEachResilient { it.block(length) }
        return length
    }

    private fun deleteInBatch(batch: LevelDB.WriteBatch, documentKey: ReadMemory) {
        Allocation.native(documentKey.size) { writeRefKeyFromDocumentKey(documentKey) } .use { refKey ->
            deleteIndexesInBatch(batch, refKey)
        }

        batch.delete(documentKey)
    }

    @Suppress("DuplicatedCode")
    override fun delete(key: ReadMemory, vararg options: Options.Write) {
        key.verifyDocumentKey()
        val anticipations = options.all<Anticipate>()
        val inLockAnticipations = options.all<AnticipateInLock>()
        val inLockReactions = options.all<ReactInLock>()
        val reactions = options.all<React>()

        anticipations.forEach { it.block() }
        ldb.newWriteBatch().use { batch ->
            lock.withLock {
                inLockAnticipations.forEach { it.block(batch) }
                deleteInBatch(batch, key)
                ldb.write(batch, LevelDB.WriteOptions.from(options))
                inLockReactions.forEachResilient { it.block(-1) }
            }
        }
        reactions.forEachResilient { it.block(-1) }
    }

    override fun newBatch(): DataBatch = DataBatchImpl(this)

    override fun newSnapshot(vararg options: Options.Read): DataSnapshot = DataSnapshotImpl(ldb, ldb.newSnapshot())

    override fun <T : Any> getExtension(key: ExtensionKey<T>): T? = null

    override fun close() {
        ldb.close()
    }
}
