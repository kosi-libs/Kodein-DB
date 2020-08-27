package org.kodein.db.model

import org.kodein.db.BaseCursor
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized

public interface ModelCursor<M : Any> : BaseCursor {

    public fun key(): Key<M>
    public fun model(vararg options: Options.Read): Sized<M>

    public fun duplicate(): ModelCursor<M>

}
