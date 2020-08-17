package org.kodein.db.data

import org.kodein.db.Options
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable

public interface DataDB : DataWrite, DataRead, Closeable {

    public val ldb: LevelDB

    public fun newBatch(): DataBatch

    public fun newSnapshot(vararg options: Options.Read): DataSnapshot

    public companion object
}
