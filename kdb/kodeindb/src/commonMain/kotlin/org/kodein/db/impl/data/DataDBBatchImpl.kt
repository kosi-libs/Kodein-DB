package org.kodein.db.impl.data

import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataWrite
import org.kodein.db.impl.utils.putBody
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.*

class DataDBBatchImpl(private val ddb: DataDBImpl) : DataDB.Batch {

    private val batch = ddb.ldb.newWriteBatch()

    private val deleteRefKeys = ArrayList<KBuffer>()

    override fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): Int {
        SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            val key = it.newSlice { putObjectKey(type, primaryKey) }
            val value = it.newSlice { putBody(body) }
            batch.put(key, value)

            val refKey = KBuffer.array(key.remaining) { putRefKeyFromObjectKey(key) }
            ddb.putIndexesInBatch(it, batch, key, refKey, indexes)

            deleteRefKeys += refKey

            return value.remaining
        }
    }

    override fun putAndGetKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): DataWrite.PutResult {
        val key = KBuffer.array(getObjectKeySize(type, primaryKey)) { putObjectKey(type, primaryKey) }

        SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            val value = it.newSlice { putBody(body) }
            batch.put(key, value)

            val refKey = KBuffer.array(key.remaining) { putRefKeyFromObjectKey(key) }
            ddb.putIndexesInBatch(it, batch, key, refKey, indexes)

            deleteRefKeys += refKey

            return DataWrite.PutResult(key, value.remaining)
        }
    }

    override fun delete(key: ReadBuffer, options: LevelDB.WriteOptions) {
        batch.delete(key)
        val refKey = KBuffer.array(key.remaining) { putRefKeyFromObjectKey(key) }
        deleteRefKeys += refKey
    }

    override fun write(options: LevelDB.WriteOptions) {
        use {
            ddb.ldb.newWriteBatch().use { fullBatch ->
                for (refKey in deleteRefKeys) {
                    ddb.deleteIndexesInBatch(fullBatch, refKey)
                }

                fullBatch.append(batch)

                ddb.ldb.write(fullBatch)
            }
        }
    }

    override fun close() = batch.close()
}
