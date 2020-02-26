package org.kodein.db.model

import org.kodein.db.BaseCursor
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized

interface ModelCursor<M : Any> : BaseCursor {

    fun key(): Key<M>
    fun model(vararg options: Options.Read): Sized<M>

    fun duplicate(): ModelCursor<M>

}
