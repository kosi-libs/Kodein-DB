package org.kodein.db.data

import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Allocation
import org.kodein.memory.Closeable
import org.kodein.memory.KBuffer
import org.kodein.memory.ReadBuffer

interface DataWrite {

    open class PutResult(val key: KBuffer, val length: Int)

    fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT): Int

    fun putAndGetKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT): PutResult

    fun delete(key: ReadBuffer, options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT)

}
