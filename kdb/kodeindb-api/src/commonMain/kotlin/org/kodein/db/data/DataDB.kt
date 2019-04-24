package org.kodein.db.data

import org.kodein.db.Value
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Allocation
import org.kodein.memory.Closeable
import org.kodein.memory.KBuffer
import org.kodein.memory.ReadBuffer

interface DataDB : DataWrite, Closeable {

    val ldb: LevelDB

    interface Batch : DataWrite, Closeable {
        fun write(options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT)
    }

    fun get(key: ReadBuffer, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): Allocation?

    fun findAll(options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataCursor

    fun findAllByType(type: String, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataCursor

    fun findByPrimaryKeyPrefix(type: String, primaryKey: Value, isOpen: Boolean = false, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataCursor

    fun findAllByIndex(type: String, name: String, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataCursor

    fun findByIndexPrefix(type: String, name: String, value: Value, isOpen: Boolean = false, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataCursor

    fun findIndexes(key: ReadBuffer, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): List<String>

    fun newBatch(): Batch

    fun getKey(type: String, primaryKey: Value): KBuffer
}
