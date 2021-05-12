package org.kodein.db.data

import org.kodein.db.Body
import org.kodein.db.ExtensionKey
import org.kodein.db.Options
import org.kodein.db.kv.KeyValueDB
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory

public interface DataDB : DataWrite, DataRead, Closeable {

    public val kv: KeyValueDB

    override fun put(key: ReadMemory, body: Body, indexes: DataIndexMap, vararg options: Options.Puts): Int = put(key, body, indexes, *(options as Array<out Options.DirectPut>))
    public fun put(key: ReadMemory, body: Body, indexes: DataIndexMap = emptyMap(), vararg options: Options.DirectPut): Int

    override fun delete(key: ReadMemory, vararg options: Options.Deletes) { delete(key, *(options as Array<out Options.DirectDelete>)) }
    public fun delete(key: ReadMemory, vararg options: Options.DirectDelete)

    public fun newBatch(vararg options: Options.NewBatch): DataBatch

    public fun newSnapshot(vararg options: Options.NewSnapshot): DataSnapshot

    public fun <T: Any> getExtension(key: ExtensionKey<T>): T?

    public companion object
}
