package org.kodein.db.model

import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory

public interface ModelDBListener<in M : Any> {

    public fun setSubscription(subscription: Closeable) {}

    public fun willPut(model: M, key: Key<M>, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Puts>) {}

    public fun didPut(model: M, key: Key<M>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) {}

    public fun willDelete(key: Key<M>, getModel: () -> M?, typeName: ReadMemory, options: Array<out Options.Deletes>) {}

    public fun didDelete(key: Key<M>, model: M?, typeName: ReadMemory, options: Array<out Options.Deletes>) {}

}
