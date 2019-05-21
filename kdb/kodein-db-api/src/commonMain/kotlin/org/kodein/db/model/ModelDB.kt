package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.memory.Closeable

interface ModelDB : ModelWrite, ModelRead, Closeable {

    interface Batch : ModelWrite, Closeable {
        fun write(vararg options: Options.Write)
    }

    fun newBatch(): Batch

    interface Snapshot : ModelRead, Closeable

    fun newSnapshot(): Snapshot

    class OpenOptions(
            val serializer: Serializer? = null,
            val metadataExtractor: MetadataExtractor? = null,
            val typeTable: TypeTable? = null
    ) : Options.Open

}

