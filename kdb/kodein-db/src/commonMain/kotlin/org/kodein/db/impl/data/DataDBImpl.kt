package org.kodein.db.impl.data

import org.kodein.db.*
import org.kodein.db.data.DataBatch
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataSnapshot
import org.kodein.db.data.DataWrite
import org.kodein.db.impl.utils.newLock
import org.kodein.db.impl.utils.putBody
import org.kodein.db.impl.utils.withLock
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.SliceBuilder
import org.kodein.memory.use
import org.kodein.memory.util.forEachResilient

internal class DataDBImpl(override val ldb: LevelDB) : DataReadModule, DataDB {

    override val snapshot: LevelDB.Snapshot? get() = null

    internal val lock = newLock()

    companion object {
        internal const val DEFAULT_CAPACITY = 16384

        internal fun toLdb(options: Array<out Options.Write>): LevelDB.WriteOptions {
            val syncOption: DataWrite.Sync = options() ?: return LevelDB.WriteOptions.DEFAULT
            return LevelDB.WriteOptions(
                    sync = syncOption.sync
            )
        }
    }

    internal fun deleteIndexesInBatch(batch: LevelDB.WriteBatch, refKey: ReadBuffer) {
        val indexes = ldb.get(refKey) ?: return

        indexes.use {
            while (indexes.valid()) {
                val len = indexes.readInt()
                val indexKey = indexes.slice(indexes.position, len)
                batch.delete(indexKey)
                indexes.skip(len)
            }
        }

        batch.delete(refKey)
    }

    internal fun putIndexesInBatch(sb: SliceBuilder, batch: LevelDB.WriteBatch, key: ReadMemory, refKey: ReadBuffer, indexes: Set<Index>) {
        if (indexes.isEmpty())
            return

        val ref = sb.newSlice {
            for (index in indexes) {
                val indexValue = Value.ofAny(index.value)
                val indexKeySize = getIndexKeySize(key, index.name, indexValue)
                putInt(indexKeySize)
                val indexKey = subSlice { putIndexKey(key, index.name, indexValue) }
                batch.put(indexKey, key)
            }
        }

        batch.put(refKey, ref)
    }

    private fun putInBatch(sb: SliceBuilder, batch: LevelDB.WriteBatch, key: ReadMemory, body: Body, indexes: Set<Index>): Int {
        val refKey = sb.newSlice {
            putRefKeyFromDocumentKey(key)
        }

        deleteIndexesInBatch(batch, refKey)
        putIndexesInBatch(sb, batch, key, refKey, indexes)

        val value = sb.newSlice { putBody(body) }
        batch.put(key, value)

        return value.available
    }

    override fun put(key: ReadMemory, body: Body, indexes: Set<Index>, vararg options: Options.Write): Int {
        key.verifyDocumentKey()
        SliceBuilder.native(DEFAULT_CAPACITY).use { sb ->
            val checks = options.all<Anticipate>()
            val reacts = options.all<React>()

            checks.filter { it.needsLock.not() } .forEach { it.block() }
            val length = ldb.newWriteBatch().use { batch ->
                lock.withLock {
                    checks.filter { it.needsLock } .forEach { it.block() }
                    val length = putInBatch(sb, batch, key, body, indexes)
                    ldb.write(batch, toLdb(options))
                    reacts.filter { it.needsLock } .forEachResilient { it.block(length) }
                    length
                }
            }
            reacts.filter { it.needsLock.not() } .forEachResilient { it.block(length) }
            return length
//            return put(it, key, body, indexes, *options)
        }
    }

    private fun deleteInBatch(sb: SliceBuilder, batch: LevelDB.WriteBatch, key: ReadMemory) {
        val refKey = sb.newSlice { putRefKeyFromDocumentKey(key) }

        deleteIndexesInBatch(batch, refKey)
        batch.delete(key)
    }

    override fun delete(key: ReadMemory, vararg options: Options.Write) {
        key.verifyDocumentKey()
        val checks = options.all<Anticipate>()
        val reacts = options.all<React>()

        checks.filter { it.needsLock.not() } .forEach { it.block() }
        ldb.newWriteBatch().use { batch ->
            SliceBuilder.native(DEFAULT_CAPACITY).use { sb ->
                lock.withLock {
                    checks.filter { it.needsLock } .forEach { it.block() }
                    deleteInBatch(sb, batch, key)
                    ldb.write(batch, toLdb(options))
                    reacts.filter { it.needsLock } .forEachResilient { it.block(-1) }
                }
            }
        }
        reacts.filter { it.needsLock.not() } .forEachResilient { it.block(-1) }
    }

    override fun newBatch(): DataBatch = DataBatchImpl(this)

    override fun newSnapshot(vararg options: Options.Read): DataSnapshot = DataSnapshotImpl(ldb, ldb.newSnapshot())

    override fun close() {
        ldb.close()
    }
}
