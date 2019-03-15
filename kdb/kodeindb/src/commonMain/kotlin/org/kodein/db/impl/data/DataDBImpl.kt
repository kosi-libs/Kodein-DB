//package org.kodein.db.impl.data
//
//import kotlinx.io.core.discardExact
//import kotlinx.io.core.use
//import kotlinx.io.pool.DefaultPool
//import kotlinx.io.pool.ObjectPool
//import kotlinx.io.pool.useInstance
//import org.kodein.db.Body
//import org.kodein.db.Index
//import org.kodein.db.Value
//import org.kodein.db.WriteType
//import org.kodein.db.ascii.readFullyAscii
//import org.kodein.db.data.DataConcurrentModificationException
//import org.kodein.db.data.DataDB
//import org.kodein.db.data.DataIterator
//import org.kodein.db.impl.utils.makeSubView
//import org.kodein.db.impl.utils.makeViewOf
//import org.kodein.db.impl.utils.writeFully
//import org.kodein.db.leveldb.Allocation
//import org.kodein.db.leveldb.Bytes
//import org.kodein.db.leveldb.LevelDB
//
//class DataDBImpl(override val ldb: LevelDB) : DataDB {
//
//    private val pool: ObjectPool<Bytes> = object : DefaultPool<Bytes>(8) {
//        override fun produceInstance(): Bytes = Allocation.allocNativeBuffer(16384)
//
//        override fun disposeInstance(instance: Bytes) {
//            (instance as Allocation).close()
//        }
//
//        override fun clearInstance(instance: Bytes): Bytes {
//            instance.buffer.resetForWrite()
//            return instance
//        }
//
//        override fun validateInstance(instance: Bytes) {}
//    }
//
//    override fun get(key: Bytes, options: LevelDB.ReadOptions): DataDB.Versioned? {
//        val allocation = ldb.get(key, options) ?: return null
//        val version = allocation.buffer.readInt()
//        return DataDB.Versioned(version, allocation)
//    }
//
//    override fun findAll(options: LevelDB.ReadOptions): DataIterator {
//        return DataSimpleIterator(ldb.newCursor(options), objectEmptyPrefix)
//    }
//
//    override fun findAllByType(type: String, options: LevelDB.ReadOptions): DataIterator {
//        pool.useInstance {
//            it.writeObjectKey(type, null)
//            return DataSimpleIterator(ldb.newCursor(options), it)
//        }
//    }
//
//    override fun findByPrimaryKeyPrefix(type: String, primaryKey: Value, isOpen: Boolean, options: LevelDB.ReadOptions): DataIterator {
//        pool.useInstance {
//            it.writeObjectKey(type, primaryKey, isOpen)
//            return DataSimpleIterator(ldb.newCursor(options), it)
//        }
//    }
//
//    override fun findAllByIndex(type: String, name: String, options: LevelDB.ReadOptions): DataIterator {
//        pool.useInstance {
//            it.writeIndexKeyStart(type, name, null)
//            return DataSimpleIterator(ldb.newCursor(options), it)
//        }
//    }
//
//    override fun findByIndexPrefix(type: String, name: String, value: Value, isOpen: Boolean, options: LevelDB.ReadOptions): DataIterator {
//        pool.useInstance {
//            it.writeIndexKeyStart(type, name, value, isOpen)
//            return DataSimpleIterator(ldb.newCursor(options), it)
//        }
//    }
//
//    private fun deleteIndexesInBatch(batch: LevelDB.WriteBatch, refKey: Bytes) {
//        val indexes = ldb.get(refKey) ?: return
//
//        indexes.use {
//            while (indexes.buffer.canRead()) {
//                val len = indexes.buffer.readInt()
//                val indexKey = indexes.makeSubView(0, len)
//                indexes.buffer.discardExact(len)
//
//                batch.delete(indexKey)
//            }
//        }
//
//        batch.delete(refKey)
//    }
//
//    override fun version(key: Bytes, options: LevelDB.ReadOptions): Int {
//        val bytes = ldb.get(key, options) ?: return 0
//
//        return bytes.use {
//            it.buffer.readInt()
//        }
//    }
//
//    private fun putIndexesInBatch(dst: Bytes, batch: LevelDB.WriteBatch, objectKey: Bytes, refKey: Bytes, indexes: Set<Index>) {
//        if (indexes.isEmpty())
//            return
//
//        val ref = dst.makeViewOf {
//            for (index in indexes) {
//                buffer.writeInt(getIndexKeySize(objectKey, index.name, index.value))
//                val indexKey = makeViewOf { writeIndexKey(objectKey, index.name, index.value) }
//                batch.put(indexKey, objectKey)
//            }
//        }
//
//        batch.put(refKey, ref)
//    }
//
//    private fun putInBatch(dst: Bytes, batch: LevelDB.WriteBatch, type: String, objectKey: Bytes, body: Body, indexes: Set<Index>, expectedVersion: Int): DataDB.PutResult {
//        val version = version(objectKey)
//
//        if (expectedVersion >= 0 && version != expectedVersion)
//            throw DataConcurrentModificationException(type, expectedVersion, version, WriteType.PUT, objectKey)
//
//        val newVersion = version + 1
//
//        val refKey = dst.makeViewOf { writeRefKeyFromObjectKey(objectKey) }
//
//        deleteIndexesInBatch(batch, refKey)
//        putIndexesInBatch(dst, batch, objectKey, refKey, indexes)
//
//        val value = dst.makeViewOf {
//            buffer.writeInt(newVersion)
//            buffer.writeFully(body)
//        }
//        batch.put(objectKey, value)
//
//        return DataDB.PutResult(objectKey, version, value.buffer.readRemaining)
//    }
//
//    override fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, expectedVersion: Int, options: LevelDB.WriteOptions): DataDB.PutResult {
//        pool.useInstance { dst ->
//            val objectKey = dst.makeViewOf { writeObjectKey(type, primaryKey) }
//            return ldb.newWriteBatch().use { batch ->
//                val result = putInBatch(dst, batch, type, objectKey, body, indexes, expectedVersion)
//                ldb.write(batch, options)
//                result
//            }
//        }
//    }
//
//    private fun deleteInBatch(dst: Bytes, batch: LevelDB.WriteBatch, type: Bytes, objectKey: Bytes, expectedVersion: Int) {
//        if (expectedVersion >= 0) {
//            val version: Int = version(objectKey)
//
//            if (version != expectedVersion)
//                throw DataConcurrentModificationException(type.buffer.makeView().readFullyAscii(), expectedVersion, version, WriteType.DELETE, objectKey)
//        }
//
//        val refKey = dst.makeViewOf { writeRefKeyFromObjectKey(objectKey) }
//
//        deleteIndexesInBatch(batch, refKey)
//        batch.delete(objectKey)
//    }
//
//    override fun delete(objectKey: Bytes, expectedVersion: Int, options: LevelDB.WriteOptions) {
//        val type = getObjectKeyType(objectKey)
//
//        pool.useInstance { dst ->
//            return ldb.newWriteBatch().use { batch ->
//                deleteInBatch(dst, batch, type, objectKey, expectedVersion)
//                ldb.write(batch, options)
//            }
//        }
//    }
//
//    override fun newSnapshot(): LevelDB.Snapshot = ldb.newSnapshot()
//
//    override fun findIndexes(objectKey: Bytes, options: LevelDB.ReadOptions): List<String> {
//        val list = ArrayList<String>()
//
//        val indexes = pool.useInstance { dst ->
//            val refKey = dst.makeViewOf { writeRefKeyFromObjectKey(objectKey) }
//            ldb.get(refKey, options) ?: return list
//        }
//
//        indexes.use {
//            while (indexes.buffer.canRead()) {
//                val len = indexes.buffer.readInt()
//                val indexKey = indexes.makeSubView(0, len)
//                indexes.buffer.discardExact(len)
//
//                val type = getIndexKeyName(indexKey)
//                list.add(type.buffer.readFullyAscii())
//            }
//        }
//
//        return list
//    }
//
//    override fun close() {
//        ldb.close()
//    }
//}
