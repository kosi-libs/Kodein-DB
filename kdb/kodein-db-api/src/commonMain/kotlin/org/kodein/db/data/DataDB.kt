package org.kodein.db.data

import org.kodein.db.Options
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable

interface DataDB : DataWrite, DataRead, Closeable {

    val ldb: LevelDB

    fun newBatch(): DataBatch

    fun newSnapshot(vararg options: Options.Read): DataSnapshot

    companion object
}
