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

class DataDBBatchImpl(private val ddb: DataDBImpl) : DataDB.Batch {

    private val batch = ddb.ldb.newWriteBatch()

    private val deleteKeys = ArrayList<ByteArray>()

//    private fun putInBatch(dst: Bytes, batch: LevelDB.WriteBatch, key: Bytes, body: Body, indexes: Set<Index>): Int {
//        val refKey = dst.makeViewOf { writeRefKeyFromObjectKey(key) }
//
//        deleteIndexesInBatch(batch, refKey)
//        putIndexesInBatch(dst, batch, key, refKey, indexes)
//
//        val value = dst.makeViewOf {
//            buffer.writeFully(body)
//        }
//        batch.put(key, value)
//
//
//        return value.buffer.readRemaining
//    }

    override fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): Int {
        ddb.pool.useInstance { dst ->
            val key = dst.makeViewOf { writeObjectKey(type, primaryKey) }
            val value = dst.makeViewOf { buffer.writeFully(body) }
            batch.put(key, value)

            deleteKeys += key.makeView().readBytes()

            return value.buffer.readRemaining
        }
    }

    override fun putAndGetKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): DataWrite.PutResult {
        val dst = ddb.pool.borrow()
        val key = dst.makeViewOf { writeObjectKey(type, primaryKey) }
        val value = dst.makeViewOf { buffer.writeFully(body) }
        batch.put(key, value)

        deleteKeys += key.makeView().readBytes()

        return DataWrite.PutResult(ddb.ViewFromPool(dst, key), value.buffer.readRemaining)
    }

    override fun delete(key: Bytes, options: LevelDB.WriteOptions) {
        ddb.pool.useInstance { dst ->
            ddb.deleteInBatch(dst, batch, key)
        }
    }

    override fun write(options: LevelDB.WriteOptions) {
        use {
            for (key in deleteKeys) {

            }

            ddb.ldb.write(batch)
        }
    }

    override fun close() = batch.close()
}
