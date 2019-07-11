package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.react.DBListener
import org.kodein.memory.Closeable

interface ModelDB : ModelWrite, ModelRead, Closeable {

    class OpenOptions(
            val serializer: Serializer? = null,
            val metadataExtractor: MetadataExtractor? = null,
            val typeTable: TypeTable? = null
    ) : Options.Open

    interface Batch : ModelWrite, Closeable {
        fun write(vararg options: Options.Write)
    }

    fun newBatch(): Batch

    interface Snapshot : ModelRead, Closeable

    fun newSnapshot(vararg options: Options.Read): Snapshot

    fun register(listener: DBListener, vararg options: Options.React): Closeable
}

