package org.kodein.db.impl.data

import kotlinx.io.core.use
import kotlinx.io.pool.useInstance
import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataWrite
import org.kodein.db.impl.utils.makeViewOf
import org.kodein.db.impl.utils.writeFully
import org.kodein.db.leveldb.Bytes
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.readBytes
import org.kodein.db.leveldb.write

class DataDBBatchImpl(private val ddb: DataDBImpl) : DataDB.Batch {

    private val batch = ddb.ldb.newWriteBatch()

    private val deleteRefKeys = ArrayList<ByteArray>()

    override fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): Int {
        ddb.pool.useInstance { dst ->
            val key = dst.makeViewOf { writeObjectKey(type, primaryKey) }
            val value = dst.makeViewOf { buffer.writeFully(body) }
            batch.put(key, value)

            val refKey = dst.makeViewOf { writeRefKeyFromObjectKey(key) }
            ddb.putIndexesInBatch(dst, batch, key, refKey, indexes)

            deleteRefKeys += refKey.makeView().readBytes()

            return value.buffer.readRemaining
        }
    }

    override fun putAndGetKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): DataWrite.PutResult {
        val dst = ddb.pool.borrow()
        val key = dst.makeViewOf { writeObjectKey(type, primaryKey) }
        val value = dst.makeViewOf { buffer.writeFully(body) }
        batch.put(key, value)

        val refKey = dst.makeViewOf { writeRefKeyFromObjectKey(key) }
        ddb.putIndexesInBatch(dst, batch, key, refKey, indexes)

        deleteRefKeys += refKey.makeView().readBytes()

        return DataWrite.PutResult(ddb.ViewFromPool(dst, key), value.buffer.readRemaining)
    }

    override fun delete(key: Bytes, options: LevelDB.WriteOptions) {
        batch.delete(key)
        ddb.pool.useInstance { dst ->
            val refKey = dst.makeViewOf { writeRefKeyFromObjectKey(key) }
            deleteRefKeys += refKey.makeView().readBytes()
        }
    }

    override fun write(options: LevelDB.WriteOptions) {
        use {
            ddb.ldb.newWriteBatch().use { fullBatch ->
                for (refKeyArray in deleteRefKeys) {
                    ddb.pool.useInstance { dst ->
                        val refKey = dst.makeViewOf { write(refKeyArray) }
                        ddb.deleteIndexesInBatch(fullBatch, refKey)
                    }
                }

                fullBatch.append(batch)

                ddb.ldb.write(fullBatch)
            }
        }
    }

    override fun close() = batch.close()
}
