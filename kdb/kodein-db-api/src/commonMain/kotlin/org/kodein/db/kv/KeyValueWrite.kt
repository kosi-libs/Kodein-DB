package org.kodein.db.kv

import org.kodein.db.Options
import org.kodein.memory.io.ReadMemory


public interface KeyValueWrite {

    public fun put(key: ReadMemory, value: ReadMemory) { put(key, value, *emptyArray()) }
    public fun put(key: ReadMemory, value: ReadMemory, vararg options: Options.Puts)

    public fun delete(key: ReadMemory) { delete(key, *emptyArray()) }
    public fun delete(key: ReadMemory, vararg options: Options.Deletes)

}
