package org.kodein.db.data

import kotlinx.io.core.Closeable
import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.leveldb.Allocation
import org.kodein.db.leveldb.Bytes
import org.kodein.db.leveldb.LevelDB

interface DataDB : Closeable {

    val ldb: LevelDB

    data class Versioned(val version: Int, val allocation: Allocation) : Closeable {
        override fun close() = allocation.close()
    }

    data class PutResult(val objectKey: Bytes, val version: Int, val length: Int)


    fun get(key: Bytes, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): Versioned?

    fun findAll(options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun findAllByType(type: String, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun findByPrimaryKeyPrefix(type: String, primaryKey: Value, isOpen: Boolean = false, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun findAllByIndex(type: String, name: String, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun findByIndexPrefix(type: String, name: String, value: Value, isOpen: Boolean = false, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): DataIterator

    fun version(key: Bytes, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): Int

    fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), expectedVersion: Int = -1, options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT): PutResult

    fun delete(objectKey: Bytes, expectedVersion: Int = -1, options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT)

    fun newSnapshot(): LevelDB.Snapshot

    fun findIndexes(objectKey: Bytes, options: LevelDB.ReadOptions): List<String>

}
