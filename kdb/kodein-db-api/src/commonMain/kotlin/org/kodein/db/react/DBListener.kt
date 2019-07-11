package org.kodein.db.react

import org.kodein.db.model.Key
import org.kodein.db.model.Metadata
import org.kodein.memory.Closeable

interface DBListener {

    fun setSubscription(subscription: Closeable) {}

    fun willPut(model: Any, typeName: String, metadata: Metadata) {}

    fun didPut(model: Any, typeName: String, metadata: Metadata) {}

    fun willDelete(key: Key<*>, typeName: String, getModel: () -> Any?) {}

    fun didDelete(key: Key<*>, typeName: String) {}

}
