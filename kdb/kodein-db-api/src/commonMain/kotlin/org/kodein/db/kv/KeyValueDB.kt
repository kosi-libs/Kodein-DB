package org.kodein.db.kv

import org.kodein.db.Options
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory


public interface KeyValueDB : KeyValueRead, KeyValueWrite, Closeable {
    public val path: String

    public val ldb: LevelDB

    override fun put(key: ReadMemory, value: ReadMemory, vararg options: Options.Puts) { put(key, value, *(options as Array<out Options.DirectPut>)) }
    public fun put(key: ReadMemory, value: ReadMemory, vararg options: Options.DirectPut)

    override fun delete(key: ReadMemory, vararg options: Options.Deletes) { delete(key, *(options as Array<out Options.DirectDelete>)) }
    public fun delete(key: ReadMemory, vararg options: Options.DirectDelete)

    public fun newSnapshot(vararg options: Options.NewSnapshot): KeyValueSnapshot

    public fun newBatch(vararg options: Options.NewBatch): KeyValueBatch

    public companion object

}
