package org.kodein.db.impl.data

import kotlinx.io.core.*
import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool
import kotlinx.io.pool.useInstance
import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.WriteType
import org.kodein.db.ascii.readFullyAscii
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataIterator
import org.kodein.db.data.DataWrite
import org.kodein.db.impl.utils.makeSubView
import org.kodein.db.impl.utils.makeViewOf
import org.kodein.db.impl.utils.writeFully
import org.kodein.db.leveldb.Allocation
import org.kodein.db.leveldb.Bytes
import org.kodein.db.leveldb.LevelDB

class DataDBImpl(override val ldb: LevelDB) : DataDB {

    internal val pool: ObjectPool<Bytes> = object : DefaultPool<Bytes>(8) {
        override fun produceInstance(): Bytes = Allocation.allocNativeBuffer(16384)

        override fun disposeInstance(instance: Bytes) {
            (instance as Allocation).close()
        }

        override fun clearInstance(instance: Bytes): Bytes {
            instance.buffer.resetForWrite()
            return instance
        }

        override fun validateInstance(instance: Bytes) {}
    }

    override fun get(key: Bytes, options: LevelDB.ReadOptions): Allocation? = ldb.get(key, options)

    override fun findAll(options: LevelDB.ReadOptions): DataIterator = DataSimpleIterator(ldb.newCursor(options), objectEmptyPrefix)

    override fun findAllByType(type: String, options: LevelDB.ReadOptions): DataIterator {
        pool.useInstance {
            val key = it.makeViewOf { it.writeObjectKey(type, null) }
            return DataSimpleIterator(ldb.newCursor(options), key)
        }
    }

    override fun findByPrimaryKeyPrefix(type: String, primaryKey: Value, isOpen: Boolean, options: LevelDB.ReadOptions): DataIterator {
        pool.useInstance {
            val key = it.makeViewOf { it.writeObjectKey(type, primaryKey, isOpen) }
            return DataSimpleIterator(ldb.newCursor(options), key)
        }
    }

    override fun findAllByIndex(type: String, name: String, options: LevelDB.ReadOptions): DataIterator {
        pool.useInstance {
            val key = it.makeViewOf { it.writeIndexKeyStart(type, name, null) }
            return DataIndexIterator(this, ldb.newCursor(options), key, options)
        }
    }

    override fun findByIndexPrefix(type: String, name: String, value: Value, isOpen: Boolean, options: LevelDB.ReadOptions): DataIterator {
        pool.useInstance {
            val key = it.makeViewOf { it.writeIndexKeyStart(type, name, value, isOpen) }
            return DataIndexIterator(this, ldb.newCursor(options), key, options)
        }
    }

    internal fun deleteIndexesInBatch(batch: LevelDB.WriteBatch, refKey: Bytes) {
        val indexes = ldb.get(refKey) ?: return

        indexes.use {
            while (indexes.buffer.canRead()) {
                val len = indexes.buffer.readInt()
                val indexKey = indexes.makeSubView(0, len)
                indexes.buffer.discardExact(len)

                batch.delete(indexKey)
            }
        }

        batch.delete(refKey)
    }

    internal fun putIndexesInBatch(dst: Bytes, batch: LevelDB.WriteBatch, key: Bytes, refKey: Bytes, indexes: Set<Index>) {
        if (indexes.isEmpty())
            return

        val ref = dst.makeViewOf {
            for (index in indexes) {
                buffer.writeInt(getIndexKeySize(key, index.name, index.value))
                val indexKey = makeViewOf { writeIndexKey(key, index.name, index.value) }
                batch.put(indexKey, key)
            }
        }

        batch.put(refKey, ref)
    }

    private fun putInBatch(dst: Bytes, batch: LevelDB.WriteBatch, key: Bytes, body: Body, indexes: Set<Index>): Int {
        val refKey = dst.makeViewOf { writeRefKeyFromObjectKey(key) }

        deleteIndexesInBatch(batch, refKey)
        putIndexesInBatch(dst, batch, key, refKey, indexes)

        val value = dst.makeViewOf { buffer.writeFully(body) }
        batch.put(key, value)

        return value.buffer.readRemaining
    }

    override fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): Int {
        pool.useInstance { dst ->
            val key = dst.makeViewOf { writeObjectKey(type, primaryKey) }
            ldb.newWriteBatch().use { batch ->
                val length = putInBatch(dst, batch, key, body, indexes)
                ldb.write(batch, options)
                return length
            }
        }
    }

    override fun putAndGetKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, options: LevelDB.WriteOptions): DataWrite.PutResult {
        val dst = pool.borrow()
        val key = dst.makeViewOf { writeObjectKey(type, primaryKey) }
        val length = ldb.newWriteBatch().use { batch ->
            val length = putInBatch(dst, batch, key, body, indexes)
            ldb.write(batch, options)
            length
        }
        return DataWrite.PutResult(ViewFromPool(dst, key), length)
    }

    private fun deleteInBatch(dst: Bytes, batch: LevelDB.WriteBatch, key: Bytes) {
        val refKey = dst.makeViewOf { this.writeRefKeyFromObjectKey(key) }

        deleteIndexesInBatch(batch, refKey)
        batch.delete(key)
    }

    override fun delete(key: Bytes, options: LevelDB.WriteOptions) {
        pool.useInstance { dst ->
            return ldb.newWriteBatch().use { batch ->
                deleteInBatch(dst, batch, key)
                ldb.write(batch, options)
            }
        }
    }

    override fun findIndexes(key: Bytes, options: LevelDB.ReadOptions): List<String> {
        val list = ArrayList<String>()

        val indexes = pool.useInstance { dst ->
            val refKey = dst.makeViewOf { writeRefKeyFromObjectKey(key) }
            ldb.get(refKey, options) ?: return list
        }

        indexes.use {
            while (indexes.buffer.canRead()) {
                val len = indexes.buffer.readInt()
                val indexKey = indexes.makeSubView(0, len)
                indexes.buffer.discardExact(len)

                val type = getIndexKeyName(indexKey)
                list.add(type.buffer.readFullyAscii())
            }
        }

        return list
    }

    override fun newBatch(): DataDB.Batch = DataDBBatchImpl(this)

    internal inner class ViewFromPool(private val orig: Bytes, private val view: Bytes) : Allocation, Bytes by view {
        override fun close() {
            pool.recycle(orig)
        }
    }

    override fun allocKey(type: String, primaryKey: Value): Allocation {
        val dst = pool.borrow()
        val view = dst.makeViewOf { writeObjectKey(type, primaryKey) }
        return ViewFromPool(dst, view)
    }

    override fun alloc(bytes: ByteArray): Allocation {
        val dst = pool.borrow()
        val view = dst.makeViewOf { buffer.writeFully(bytes) }
        return ViewFromPool(dst, view)
    }

    override fun close() {
        ldb.close()
    }
}
