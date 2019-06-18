package org.kodein.db.data

import org.kodein.db.Options
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable

interface DataDB : DataWrite, DataRead, Closeable {

    val ldb: LevelDB

    interface Batch : DataWrite, Closeable {
        fun write(vararg options: Options.Write)
    }

    fun newBatch(): Batch

    interface Snapshot : DataRead, Closeable

    fun newSnapshot(vararg options: Options.Read): Snapshot

}
