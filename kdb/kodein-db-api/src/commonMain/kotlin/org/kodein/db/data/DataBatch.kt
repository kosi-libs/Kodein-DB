package org.kodein.db.data

import org.kodein.db.Body
import org.kodein.db.Options
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.util.MaybeThrowable


public interface DataBatch : DataWrite, Closeable {
    override fun put(key: ReadMemory, body: Body, indexes: DataIndexMap, vararg options: Options.Puts): Int = put(key, body, indexes, *(options as Array<out Options.BatchPut>))
    public fun put(key: ReadMemory, body: Body, indexes: DataIndexMap = emptyMap(), vararg options: Options.BatchPut): Int

    override fun delete(key: ReadMemory, vararg options: Options.Deletes) { delete(key, *(options as Array<out Options.BatchDelete>)) }
    public fun delete(key: ReadMemory, vararg options: Options.BatchDelete)

    public fun write(afterErrors: MaybeThrowable, vararg options: Options.BatchWrite)
}
