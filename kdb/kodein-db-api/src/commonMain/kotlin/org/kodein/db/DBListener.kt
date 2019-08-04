package org.kodein.db

import org.kodein.db.model.Metadata
import org.kodein.memory.Closeable

interface DBListener {

    fun setSubscription(subscription: Closeable) {}

    fun willPut(model: Any, typeName: String, metadata: Metadata, options: Array<out Options.Write>) {}

    fun didPut(model: Any, getKey: () -> Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) {}

    fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: String, options: Array<out Options.Write>) {}

    fun didDelete(key: Key<*>, model: Any?, typeName: String, options: Array<out Options.Write>) {}

    class Builder {
        class WillPut(val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable)

        class DidPut(private val getKey: () -> Key<*>, val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable) {
            val key get() = getKey()
        }

        class WillDelete(val key: Key<*>, val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable)

        class DidDelete(val key: Key<*>, val typeName: String, val options: Array<out Options.Write>)

        private var willPut: (WillPut.(Any) -> Unit)? = null
        private var didPut: (DidPut.(Any) -> Unit)? = null

        private var willDelete: (WillDelete.(() -> Any?) -> Unit)? = null
        private var didDelete: (DidDelete.(Any?) -> Unit)? = null

        private var didDeleteNeedsModel = false

        fun willPut(block: WillPut.(Any) -> Unit) {
            willPut = block
        }

        fun didPut(block: DidPut.(Any) -> Unit) {
            didPut = block
        }

        fun willDelete(block: WillDelete.() -> Unit) {
            willDelete = { block() }
        }

        fun willDelete(block: WillDelete.(Any) -> Unit) {
            willDelete = { it()?.let { block(it) } }
        }

        fun didDelete(block: DidDelete.() -> Unit) {
            didDelete = { block() }
        }

        fun didDelete(block: DidDelete.(Any) -> Unit) {
            didDeleteNeedsModel = true
            didDelete = { it?.let { block(it) } }
        }

        fun build() = object : DBListener {
            private lateinit var subscription: Closeable

            override fun setSubscription(subscription: Closeable) {
                this.subscription = subscription
            }

            override fun willPut(model: Any, typeName: String, metadata: Metadata, options: Array<out Options.Write>) {
                willPut?.invoke(WillPut(typeName, options, subscription), model)
            }

            override fun didPut(model: Any, getKey: () -> Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) {
                didPut?.invoke(DidPut(getKey, typeName, options, subscription), model)
            }

            override fun willDelete(key: Key<*>, getModel: () -> Any?, typeName: String, options: Array<out Options.Write>) {
                if (didDeleteNeedsModel)
                    getModel()
                willDelete?.invoke(WillDelete(key, typeName, options, subscription), getModel)
            }

            override fun didDelete(key: Key<*>, model: Any?, typeName: String, options: Array<out Options.Write>) {
                didDelete?.invoke(DidDelete(key, typeName, options), model)
            }
        }

    }

}
