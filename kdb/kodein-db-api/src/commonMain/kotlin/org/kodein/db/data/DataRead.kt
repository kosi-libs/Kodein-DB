package org.kodein.db.data

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.memory.Allocation
import org.kodein.memory.KBuffer
import org.kodein.memory.ReadBuffer

interface DataRead : DataBase {

    fun get(key: ReadBuffer, vararg options: Options.Read): Allocation?

    fun findAll(vararg options: Options.Read): DataCursor

    fun findAllByType(type: String, vararg options: Options.Read): DataCursor

    fun findByPrimaryKey(type: String, primaryKey: Value, isOpen: Boolean = false, vararg options: Options.Read): DataCursor

    fun findAllByIndex(type: String, name: String, vararg options: Options.Read): DataCursor

    fun findByIndex(type: String, name: String, value: Value, isOpen: Boolean = false, vararg options: Options.Read): DataCursor

    fun getIndexesOf(key: ReadBuffer, vararg options: Options.Read): List<String>
}
