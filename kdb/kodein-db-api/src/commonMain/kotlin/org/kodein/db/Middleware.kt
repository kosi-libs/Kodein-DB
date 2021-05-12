package org.kodein.db

import org.kodein.db.data.DataDB
import org.kodein.db.kv.KeyValueDB
import org.kodein.db.model.ModelDB


public interface Middleware : Options.Open {

    public fun interface KeyValue : Middleware { public fun wrap(kvdb: KeyValueDB): KeyValueDB }
    public fun interface Data : Middleware { public fun wrap(ddb: DataDB): DataDB }
    public fun interface Model : Middleware { public fun wrap(mdb: ModelDB): ModelDB }

}
