package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.memory.Closeable
import org.kodein.memory.cache.Sized

interface DBListener {

    fun setSubscription(subscription: Closeable) {}

    fun willPut(model: Any, typeName: String, metadata: Metadata, options: Array<out Options.Write>) {}

    fun didPut(model: Any, getKey: () -> Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) {}

    fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: String, options: Array<out Options.Write>) {}

    fun didDelete(key: Key<*>, model: Any?, typeName: String, options: Array<out Options.Write>) {}

}
