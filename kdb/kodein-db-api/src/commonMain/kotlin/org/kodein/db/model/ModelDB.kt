package org.kodein.db.model

import org.kodein.db.DBListener
import org.kodein.db.Options
import org.kodein.memory.Closeable

interface ModelDB : ModelWrite, ModelRead, Closeable {

    fun newBatch(): ModelBatch

    fun newSnapshot(vararg options: Options.Read): ModelSnapshot

    fun register(listener: DBListener<Any>): Closeable

    companion object
}

