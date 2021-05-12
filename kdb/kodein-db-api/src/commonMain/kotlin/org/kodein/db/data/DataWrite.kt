package org.kodein.db.data

import org.kodein.db.Body
import org.kodein.db.Options
import org.kodein.memory.io.ReadMemory

public interface DataWrite : DataKeyMaker {

    public fun put(key: ReadMemory, body: Body, indexes: DataIndexMap = emptyMap()): Int = put(key, body, indexes, *emptyArray())
    public fun put(key: ReadMemory, body: Body, indexes: DataIndexMap = emptyMap(), vararg options: Options.Puts): Int

    public fun delete(key: ReadMemory) { delete(key, *emptyArray()) }
    public fun delete(key: ReadMemory, vararg options: Options.Deletes)

}
