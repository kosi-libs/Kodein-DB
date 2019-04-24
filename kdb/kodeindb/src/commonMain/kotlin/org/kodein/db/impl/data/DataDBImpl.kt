package org.kodein.db.impl.data

import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.ascii.readAscii
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataWrite
import org.kodein.db.impl.utils.putBody
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.*

class DataDBImpl(override val ldb: LevelDB) : DataDB {

    companion object {
        internal const val DEFAULT_CAPACITY = 16384
    }

    override fun get(key: ReadBuffer, options: LevelDB.ReadOptions): Allocation? = ldb.get(key, options)

    override fun findAll(options: LevelDB.ReadOptions): DataCursor = DataSimpleCursor(ldb.newCursor(options), objectEmptyPrefix.asManagedAllocation())

    override fun findAllByType(type: String, options: LevelDB.ReadOptions): DataCursor {
        val key = Allocation.native(getObjectKeySize(type, null)) { putObjectKey(type, null) }
        return DataSimpleCursor(ldb.newCursor(options), key)
    }

    override fun findByPrimaryKeyPrefix(type: String, primaryKey: Value, isOpen: Boolean, options: LevelDB.ReadOptions): DataCursor {
        val key = Allocation.native(getObjectKeySize(type, primaryKey, isOpen)) { putObjectKey(type, primaryKey, isOpen) }
        return DataSimpleCursor(ldb.newCursor(options), key)
    }

    override fun findAllByIndex(type: String, name: String, options: LevelDB.ReadOptions): DataCursor {
        val key = Allocation.native(getIndexKeyStartSize(type, name, null)) { putIndexKeyStart(type, name, null) }
        return DataIndexCursor(this, ldb.newCursor(options), key, options)
    }

    override fun findByIndexPrefix(type: String, name: String, value: Value, isOpen: Boolean, options: LevelDB.ReadOptions): DataCursor {
        val key = Allocation.native(getIndexKeyStartSize(type, name, value, isOpen)) { putIndexKeyStart(type, name, value, isOpen) }
        return DataIndexCursor(this, ldb.newCursor(options), key, options)
    }

    internal fun deleteIndexesInBatch(batch: LevelDB.WriteBatch, refKey: ReadBuffer) {
        val indexes = ldb.get(refKey) ?: return

        indexes.use {
            while (indexes.hasRemaining()) {
                val len = indexes.readInt()
                val indexKey = indexes.slice(indexes.position, len)
                batch.delete(indexKey)
                indexes.skip(len)
            }
        }

        batch.delete(refKey)
    }

    internal fun putIndexesInBatch(sb: SliceBuilder, batch: LevelDB.WriteBatch, key: ReadBuffer, refKey: ReadBuffer, indexes: Set<Index>) {
        if (indexes.isEmpty())
            return

        val ref = sb.newSlice {
            for (index in indexes) {
                val indexKeySize = getIndexKeySize(key, index.name, index.value)
                putInt(indexKeySize)
                val indexKey = subSlice { putIndexKey(key, index.name, index.value) }
                batch.put(indexKey, key)
            }
        }

        batch.put(refKey, ref)
    }

    private fun putInBatch(sb: SliceBuilder, batch: LevelDB.WriteBatch, key: ReadBuffer, body: Body, indexes: Set<Index>): Int {
        val refKey = sb.newSlice {
            putRefKeyFromObjectKey(key)
        }

        deleteIndexesInBatch(batch, refKey)
        putIndexesInBatch(sb, batch, key, refKey, indexes)

        val value = sb.newSlice { putBody(body) }
        batch.put(key, value)

        return value.remaining
    }

    override fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): Int {
        SliceBuilder.native(DEFAULT_CAPACITY).use {
            val key = it.newSlice { putObjectKey(type, primaryKey) }
            ldb.newWriteBatch().use { batch ->
                val length = putInBatch(it, batch, key, body, indexes)
                ldb.write(batch, options)
                return length
            }
        }
    }

    override fun putAndGetKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): DataWrite.PutResult {
        val key = KBuffer.array(getObjectKeySize(type, primaryKey)) { putObjectKey(type, primaryKey) }

        ldb.newWriteBatch().use { batch ->
            SliceBuilder.native(DEFAULT_CAPACITY).use {
                val length = putInBatch(it, batch, key, body, indexes)
                ldb.write(batch, options)
                return DataWrite.PutResult(key, length)
            }
        }
    }

    private fun deleteInBatch(sb: SliceBuilder, batch: LevelDB.WriteBatch, key: ReadBuffer) {
        val refKey = sb.newSlice { putRefKeyFromObjectKey(key) }

        deleteIndexesInBatch(batch, refKey)
        batch.delete(key)
    }

    override fun delete(key: ReadBuffer, options: LevelDB.WriteOptions) {
        ldb.newWriteBatch().use { batch ->
            SliceBuilder.native(DEFAULT_CAPACITY).use {
                deleteInBatch(it, batch, key)
                ldb.write(batch, options)
            }
        }
    }

    override fun findIndexes(key: ReadBuffer, options: LevelDB.ReadOptions): List<String> {
        val indexes = SliceBuilder.native(DEFAULT_CAPACITY).use {
            val refKey = it.newSlice { putRefKeyFromObjectKey(key) }
            ldb.get(refKey, options) ?: return emptyList()
        }

        val list = ArrayList<String>()

        indexes.use {
            while (indexes.hasRemaining()) {
                val length = indexes.readInt()
                val indexKey = indexes.slice(indexes.position, length)
                indexes.skip(length)

                val type = getIndexKeyName(indexKey)
                list.add(type.readAscii())
            }
        }

        return list
    }

    override fun newBatch(): DataDB.Batch = DataDBBatchImpl(this)

    override fun getKey(type: String, primaryKey: Value): KBuffer =
            KBuffer.array(getObjectKeySize(type, primaryKey)) { putObjectKey(type, primaryKey) }

    override fun close() {
        ldb.close()
    }
}
