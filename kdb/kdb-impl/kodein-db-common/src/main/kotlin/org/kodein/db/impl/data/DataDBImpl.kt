package org.kodein.db.impl.data

import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.discardExact
import kotlinx.io.core.use
import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool
import kotlinx.io.pool.useInstance
import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.WriteType
import org.kodein.db.ascii.readFullyAscii
import org.kodein.db.data.DataConcurrentModificationException
import org.kodein.db.data.DataIterator
import org.kodein.db.impl.utils.makeSubView
import org.kodein.db.impl.utils.makeViewOf
import org.kodein.db.impl.utils.writeFully
import org.kodein.db.leveldb.Allocation
import org.kodein.db.leveldb.Bytes
import org.kodein.db.leveldb.LevelDB

class DataDBImpl(val ldb: LevelDB) : Closeable {

    private val pool: ObjectPool<Bytes> = object : DefaultPool<Bytes>(8) {
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

    data class Versioned(val version: Int, val allocation: Allocation) : Closeable {
        override fun close() = allocation.close()
    }

    fun get(key: Bytes, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): Versioned? {
        val allocation = ldb.get(key, options) ?: return null
        val version = allocation.buffer.readInt()
        return Versioned(version, allocation)
    }

    fun findAll(options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator {
        return DataSimpleIterator(ldb.newCursor(options), objectEmptyPrefix)
    }

    fun findAllByType(type: String, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataSimpleIterator {
        pool.useInstance {
            it.buffer.writeObjectKey(type, null)
            return DataSimpleIterator(ldb.newCursor(options), it)
        }
    }

    fun findByPrimaryKeyPrefix(type: String, primaryKey: Value, isOpen: Boolean = false, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataSimpleIterator {
        pool.useInstance {
            it.buffer.writeObjectKey(type, primaryKey, isOpen)
            return DataSimpleIterator(ldb.newCursor(options), it)
        }
    }

    fun findAllByIndex(type: String, name: String, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataSimpleIterator {
        pool.useInstance {
            it.buffer.writeIndexKeyStart(type, name, null)
            return DataSimpleIterator(ldb.newCursor(options), it)
        }
    }

    fun findByIndexPrefix(type: String, name: String, value: Value, isOpen: Boolean = false, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataSimpleIterator {
        pool.useInstance {
            it.buffer.writeIndexKeyStart(type, name, value, isOpen)
            return DataSimpleIterator(ldb.newCursor(options), it)
        }
    }

    private fun deleteIndexesInBatch(batch: LevelDB.WriteBatch, refKey: Bytes) {
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

    fun version(key: Bytes, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): Int {
        val bytes = ldb.get(key, options) ?: return 0

        return bytes.use {
            it.buffer.readInt()
        }
    }

    private fun putIndexesInBatch(dst: Bytes, batch: LevelDB.WriteBatch, objectKey: Bytes, refKey: Bytes, indexes: Set<Index>) {
        if (indexes.isEmpty())
            return

        val ref = dst.makeViewOf {
            for (index in indexes) {
                buffer.writeInt(getIndexKeySize(objectKey.buffer, index.name, index.value))
                val indexKey = makeViewOf { buffer.writeIndexKey(objectKey.buffer, index.name, index.value) }
                batch.put(indexKey, objectKey)
            }
        }

        batch.put(refKey, ref)
    }

    data class PutResult internal constructor(val objectKey: Bytes, val version: Int, val length: Int)

    private fun putInBatch(dst: Bytes, batch: LevelDB.WriteBatch, type: String, objectKey: Bytes, body: Body, indexes: Set<Index>, expectedVersion: Int): PutResult {
        val version = version(objectKey)

        if (expectedVersion >= 0 && version != expectedVersion)
            throw DataConcurrentModificationException(type, expectedVersion, version, WriteType.PUT, objectKey)

        val newVersion = version + 1

        val refKey = dst.makeViewOf { buffer.writeRefKeyFromObjectKey(objectKey.buffer) }

        deleteIndexesInBatch(batch, refKey)
        putIndexesInBatch(dst, batch, objectKey, refKey, indexes)

        val value = dst.makeViewOf {
            buffer.writeInt(newVersion)
            buffer.writeFully(body)
        }
        batch.put(objectKey, value)

        return PutResult(objectKey, version, value.buffer.readRemaining)
    }

    fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), expectedVersion: Int = -1, options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT): PutResult {
        pool.useInstance { dst ->
            val objectKey = dst.makeViewOf { buffer.writeObjectKey(type, primaryKey) }
            return ldb.newWriteBatch().use { batch ->
                val result = putInBatch(dst, batch, type, objectKey, body, indexes, expectedVersion)
                ldb.write(batch, options)
                result
            }
        }
    }

    private fun deleteInBatch(dst: Bytes, batch: LevelDB.WriteBatch, type: IoBuffer, objectKey: Bytes, expectedVersion: Int) {
        if (expectedVersion >= 0) {
            val version: Int = version(objectKey)

            if (version != expectedVersion)
                throw DataConcurrentModificationException(type.makeView().readFullyAscii(), expectedVersion, version, WriteType.DELETE, objectKey)
        }

        val refKey = dst.makeViewOf { buffer.writeRefKeyFromObjectKey(objectKey.buffer) }

        deleteIndexesInBatch(batch, refKey)
        batch.delete(objectKey)
    }

    fun delete(objectKey: Bytes, expectedVersion: Int = -1, options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT) {
        val type = getObjectKeyType(objectKey.buffer)

        pool.useInstance { dst ->
            return ldb.newWriteBatch().use { batch ->
                deleteInBatch(dst, batch, type, objectKey, expectedVersion)
                ldb.write(batch, options)
            }
        }
    }

    fun newSnapshot(): LevelDB.Snapshot = ldb.newSnapshot()

    fun findIndexes(objectKey: Bytes, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): List<String> {
        val list = ArrayList<String>()

        val indexes = pool.useInstance { dst ->
            val refKey = dst.makeViewOf { buffer.writeRefKeyFromObjectKey(objectKey.buffer) }
            ldb.get(refKey, options) ?: return list
        }

        indexes.use {
            while (indexes.buffer.canRead()) {
                val len = indexes.buffer.readInt()
                val indexKey = indexes.makeSubView(0, len)
                indexes.buffer.discardExact(len)

                val type = getIndexKeyName(indexKey.buffer)
                list.add(type.readFullyAscii())
            }
        }

        return list
    }

    override fun close() {
        ldb.close()
    }
}
