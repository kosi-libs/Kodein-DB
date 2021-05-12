package org.kodein.db.data

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.ReadAllocation
import org.kodein.memory.io.ReadMemory

public interface DataRead : DataKeyMaker {

    public fun get(key: ReadMemory, vararg options: Options.Get): ReadAllocation?

    public fun findAll(vararg options: Options.Find): DataCursor

    public fun findAllByType(type: Int, vararg options: Options.Find): DataCursor

    public fun findById(type: Int, id: Value, isOpen: Boolean = false, vararg options: Options.Find): DataCursor

    public fun findAllByIndex(type: Int, index: String, vararg options: Options.Find): DataIndexCursor

    public fun findByIndex(type: Int, index: String, value: Value, isOpen: Boolean = false, vararg options: Options.Find): DataIndexCursor

    public fun getIndexesOf(key: ReadMemory): Set<String>

}
