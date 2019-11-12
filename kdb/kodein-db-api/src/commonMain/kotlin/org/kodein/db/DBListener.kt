package org.kodein.db

import org.kodein.db.model.orm.Metadata
import org.kodein.memory.Closeable

interface DBListener<in M : Any> {

    fun setSubscription(subscription: Closeable) {}

    fun willPut(model: M, typeName: String, metadata: Metadata, options: Array<out Options.Write>) {}

    fun didPut(model: M, key: Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) {}

    fun willDelete(key: Key<*>, getModel: () -> M?, typeName: String, options: Array<out Options.Write>) {}

    fun didDelete(key: Key<*>, model: M?, typeName: String, options: Array<out Options.Write>) {}

    class Builder<M : Any> {
        class WillPut(val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable)

        class DidPut(val key: Key<*>, val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable)

        class WillDelete(val key: Key<*>, val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable)

        class DidDelete(val key: Key<*>, val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable)

        private var willPut: (WillPut.(M) -> Unit)? = null
        private var didPut: (DidPut.(M) -> Unit)? = null

        private var willDelete: (WillDelete.(() -> M?) -> Unit)? = null
        private var didDelete: (DidDelete.(M?) -> Unit)? = null

        private var didDeleteNeedsModel = false

        fun willPut(block: WillPut.(M) -> Unit) {
            willPut = block
        }

        fun didPut(block: DidPut.(M) -> Unit) {
            didPut = block
        }

        fun willDelete(block: WillDelete.() -> Unit) {
            willDelete = { block() }
        }

        fun willDeleteIt(block: WillDelete.(M) -> Unit) {
            willDelete = { it()?.let { block(it) } }
        }

        fun didDelete(block: DidDelete.() -> Unit) {
            didDelete = { block() }
        }

        fun didDeleteIt(block: DidDelete.(M) -> Unit) {
            didDeleteNeedsModel = true
            didDelete = { it?.let { block(it) } }
        }

        fun build() = object : DBListener<M> {
            private lateinit var subscription: Closeable

            override fun setSubscription(subscription: Closeable) {
                this.subscription = subscription
            }

            override fun willPut(model: M, typeName: String, metadata: Metadata, options: Array<out Options.Write>) {
                willPut?.invoke(WillPut(typeName, options, subscription), model)
            }

            override fun didPut(model: M, key: Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) {
                didPut?.invoke(DidPut(key, typeName, options, subscription), model)
            }

            override fun willDelete(key: Key<*>, getModel: () -> M?, typeName: String, options: Array<out Options.Write>) {
                if (didDeleteNeedsModel)
                    getModel()
                willDelete?.invoke(WillDelete(key, typeName, options, subscription), getModel)
            }

            override fun didDelete(key: Key<*>, model: M?, typeName: String, options: Array<out Options.Write>) {
                didDelete?.invoke(DidDelete(key, typeName, options, subscription), model)
            }
        }

    }

}
