package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.Value
import org.kodein.memory.Closeable
import org.kodein.memory.model.Sized
import kotlin.reflect.KClass

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

    fun <M : Any> registerOnModelChange(key: Key<M>, vararg options: Options.React, callback: (Sized<M>, React.Operation) -> Unit): Sized<M>?

    fun registerAnyChange(vararg options: Options.Read): ModelCursor<*>

}

