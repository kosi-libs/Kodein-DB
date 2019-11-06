package org.kodein.db.model

import org.kodein.db.BaseCursor
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized

interface ModelCursor<M : Any> : BaseCursor {

    fun nextEntries(size: Int): Entries<M>

    fun key(): Key<M>
    fun model(vararg options: Options.Read): Sized<M>

    interface Entries<M: Any> : BaseCursor.BaseEntries {
        fun key(i: Int): Key<M>
        operator fun get(i: Int, vararg options: Options.Read): Sized<M>
    }

}
