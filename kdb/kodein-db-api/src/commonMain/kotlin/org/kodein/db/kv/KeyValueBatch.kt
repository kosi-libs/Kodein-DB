package org.kodein.db.kv

import org.kodein.db.Options
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory


public interface KeyValueBatch : KeyValueWrite, Closeable {

    override fun put(key: ReadMemory, value: ReadMemory, vararg options: Options.Puts) { put(key, value, *(options as Array<out Options.BatchPut>)) }
    public fun put(key: ReadMemory, value: ReadMemory, vararg options: Options.BatchPut)

    override fun delete(key: ReadMemory, vararg options: Options.Deletes) { delete(key, *(options as Array<out Options.BatchDelete>)) }
    public fun delete(key: ReadMemory, vararg options: Options.BatchDelete)

    public fun clear()

    public fun append(source: KeyValueBatch)

    public fun write(vararg options: Options.BatchWrite)

}
