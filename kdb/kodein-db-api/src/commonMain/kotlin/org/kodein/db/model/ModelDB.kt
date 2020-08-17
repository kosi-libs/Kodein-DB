package org.kodein.db.model

import org.kodein.db.DBListener
import org.kodein.db.Options
import org.kodein.memory.Closeable

public interface ModelDB : ModelWrite, ModelRead, Closeable {

    public fun newBatch(): ModelBatch

    public fun newSnapshot(vararg options: Options.Read): ModelSnapshot

    public fun register(listener: DBListener<Any>): Closeable

    public companion object
}

