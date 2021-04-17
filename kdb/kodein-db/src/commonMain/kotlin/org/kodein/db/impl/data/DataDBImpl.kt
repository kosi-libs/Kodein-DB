package org.kodein.db.impl.data

import org.kodein.db.*
import org.kodein.db.data.DataBatch
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataSnapshot
import org.kodein.db.impl.utils.newLock
import org.kodein.db.impl.utils.withLock
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.SliceBuilder
import org.kodein.memory.io.asReadable
import org.kodein.memory.use
import org.kodein.memory.util.deferScope
import org.kodein.memory.util.forEachResilient

internal class DataDBImpl(override val ldb: LevelDB) : DataReadModule, DataDB {

    override val snapshot: LevelDB.Snapshot? get() = null

    internal val lock = newLock()

    companion object {
        internal const val DEFAULT_CAPACITY = 16384
    }

    internal fun deleteIndexesInBatch(batch: LevelDB.WriteBatch, refKey: ReadMemory) {
        deferScope {
            val indexes = ldb.get(refKey)?.useInScope()?.asReadable() ?: return
            while (indexes.valid()) {
                val len = indexes.readInt()
                val indexKey = indexes.readMemory(len)
                batch.delete(indexKey)
            }
        }

        batch.delete(refKey)
    }

    internal fun putIndexesInBatch(sb: SliceBuilder, batch: LevelDB.WriteBatch, key: ReadMemory, refKey: ReadMemory, indexes: Map<String, Value>) {
        if (indexes.isEmpty())
            return

        val ref = sb.slice {
            for ((name, value) in indexes) {
                val indexKeySize = getIndexKeySize(key, name, value)
                writeInt(indexKeySize)
                val indexKey = subSlice { writeIndexKey(key, name, value) }
                batch.put(indexKey, key)
            }
        }

        batch.put(refKey, ref)
    }

    private fun putInBatch(sb: SliceBuilder, batch: LevelDB.WriteBatch, key: ReadMemory, body: Body, indexes: Map<String, Value>): Int {
        val refKey = sb.slice {
            writeRefKeyFromDocumentKey(key)
        }

        deleteIndexesInBatch(batch, refKey)
        putIndexesInBatch(sb, batch, key, refKey, indexes)

        val value = sb.slice { writeBody(body) }
        batch.put(key, value)

        return value.size
    }

    @Suppress("DuplicatedCode")
    override fun put(key: ReadMemory, body: Body, indexes: Map<String, Value>, vararg options: Options.Write): Int {
        key.verifyDocumentKey()
        SliceBuilder.native(DEFAULT_CAPACITY).use { sb ->
            val anticipations = options.all<Anticipate>()
            val inLockAnticipations = options.all<AnticipateInLock>()
            val inLockReactions = options.all<ReactInLock>()
            val reactions = options.all<React>()

            anticipations.forEach { it.block() }
            val length = ldb.newWriteBatch().use { batch ->
                lock.withLock {
                    inLockAnticipations.forEach { it.block(batch) }
                    val length = putInBatch(sb, batch, key, body, indexes)
                    ldb.write(batch, LevelDB.WriteOptions.from(options))
                    inLockReactions.forEachResilient { it.block(length) }
                    length
                }
            }
            reactions.forEachResilient { it.block(length) }
            return length
//            return put(it, key, body, indexes, *options)
        }
    }

    private fun deleteInBatch(sb: SliceBuilder, batch: LevelDB.WriteBatch, key: ReadMemory) {
        val refKey = sb.slice { writeRefKeyFromDocumentKey(key) }

        deleteIndexesInBatch(batch, refKey)
        batch.delete(key)
    }

    @Suppress("DuplicatedCode")
    override fun delete(key: ReadMemory, vararg options: Options.Write) {
        key.verifyDocumentKey()
        val anticipations = options.all<Anticipate>()
        val inLockAnticipations = options.all<AnticipateInLock>()
        val inLockReactions = options.all<ReactInLock>()
        val reactions = options.all<React>()

        anticipations.forEach { it.block() }
        deferScope {
            val batch = ldb.newWriteBatch().useInScope()
            val sb = SliceBuilder.native(DEFAULT_CAPACITY).useInScope()
            lock.withLock {
                inLockAnticipations.forEach { it.block(batch) }
                deleteInBatch(sb, batch, key)
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
