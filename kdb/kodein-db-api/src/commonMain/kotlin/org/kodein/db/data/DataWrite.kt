package org.kodein.db.data

import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.memory.KBuffer
import org.kodein.memory.ReadBuffer

interface DataWrite : DataBase {

    open class PutResult(val key: KBuffer, val length: Int)

    fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), vararg options: Options.Write): Int

    fun putAndGetKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), vararg options: Options.Write): PutResult

    fun delete(key: ReadBuffer, vararg options: Options.Write)

}
