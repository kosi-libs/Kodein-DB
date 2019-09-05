package org.kodein.db.model

import org.kodein.db.*
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadBuffer

interface ModelCursor<M : Any> : BaseCursor {

    fun nextEntries(size: Int): Entries<M>

    fun transientKey(): TransientKey<M>
    fun model(vararg options: Options.Read): Sized<M>

    interface Entries<M: Any> : BaseCursor.BaseEntries {
        fun key(i: Int): Key<M>
        operator fun get(i: Int, vararg options: Options.Read): Sized<M>
    }

}
