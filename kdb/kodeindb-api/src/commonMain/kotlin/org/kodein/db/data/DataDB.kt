package org.kodein.db.data

import kotlinx.io.core.Closeable
import org.kodein.db.Value
import org.kodein.db.leveldb.Allocation
import org.kodein.db.leveldb.Bytes
import org.kodein.db.leveldb.LevelDB

interface DataDB : DataWrite, Closeable {

    interface Batch : DataWrite, Closeable {
        fun write(options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT)
    }

    fun get(key: Bytes, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): Allocation?

    fun findAll(options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun findAllByType(type: String, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun findByPrimaryKeyPrefix(type: String, primaryKey: Value, isOpen: Boolean = false, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun findAllByIndex(type: String, name: String, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun findByIndexPrefix(type: String, name: String, value: Value, isOpen: Boolean = false, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun findIndexes(key: Bytes, options: LevelDB.ReadOptions): List<String>

    fun newBatch(): Batch

    fun allocKey(type: String, primaryKey: Value): Allocation

    fun alloc(bytes: ByteArray): Allocation
}
