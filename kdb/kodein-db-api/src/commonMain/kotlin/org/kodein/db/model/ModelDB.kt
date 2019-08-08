package org.kodein.db.model

import org.kodein.db.DBListener
import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.memory.Closeable

interface ModelDB : ModelWrite, ModelRead, Closeable {

    interface Batch : ModelWrite, Closeable {
        fun write(vararg options: Options.Write)
    }

    fun newBatch(): Batch

    interface Snapshot : ModelRead, Closeable

    fun newSnapshot(vararg options: Options.Read): Snapshot

    fun register(listener: DBListener): Closeable
}

