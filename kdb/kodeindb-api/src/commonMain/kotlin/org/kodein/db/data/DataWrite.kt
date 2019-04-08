package org.kodein.db.data

import kotlinx.io.core.Closeable
import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.leveldb.Allocation
import org.kodein.db.leveldb.Bytes
import org.kodein.db.leveldb.LevelDB

interface DataWrite {

    class PutResult(val key: Allocation, val length: Int) : Closeable by key

    fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT): Int

    fun putAndGetKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT): PutResult

    fun delete(key: Bytes, options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT)

}
