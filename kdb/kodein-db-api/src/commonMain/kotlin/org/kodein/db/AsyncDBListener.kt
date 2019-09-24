package org.kodein.db

import org.kodein.db.model.orm.Metadata
import org.kodein.memory.Closeable

interface AsyncDBListener<in M : Any> {

    fun setSubscription(subscription: Closeable) {}

    suspend fun willPut(model: M, typeName: String, metadata: Metadata, options: Array<out Options.Write>) {}

    suspend fun didPut(model: M, getKey: suspend () -> Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) {}

    suspend fun willDelete(key: Key<*>, getModel: () -> M?, typeName: String, options: Array<out Options.Write>) {}

    suspend fun didDelete(key: Key<*>, model: M?, typeName: String, options: Array<out Options.Write>) {}

    class Builder<M : Any> {
        class WillPut(val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable)

        class DidPut(val getKey: suspend () -> Key<*>, val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable)

        class WillDelete(val key: Key<*>, val typeName: String, val options: Array<out Options.Write>, val subscription: Closeable)

        class DidDelete(val key: Key<*>, val typeName: String, val options: Array<out Options.Write>)

        private var willPut: (suspend  WillPut.(M) -> Unit)? = null
        private var didPut: (suspend DidPut.(M) -> Unit)? = null

        private var willDelete: (suspend WillDelete.(() -> M?) -> Unit)? = null
        private var didDelete: (suspend DidDelete.(M?) -> Unit)? = null

        private var didDeleteNeedsModel = false

        fun willPut(block: suspend WillPut.(M) -> Unit) {
            willPut = block
        }

        fun didPut(block: suspend DidPut.(M) -> Unit) {
            didPut = block
        }

        fun willDelete(block: suspend WillDelete.() -> Unit) {
            willDelete = { block() }
        }

        fun willDelete(block: suspend WillDelete.(M) -> Unit) {
            willDelete = { it()?.let { block(it) } }
        }

        fun didDelete(block: suspend DidDelete.() -> Unit) {
            didDelete = { block() }
        }

        fun didDelete(block: suspend DidDelete.(M) -> Unit) {
            didDeleteNeedsModel = true
            didDelete = { it?.let { block(it) } }
        }

        fun build() = object : AsyncDBListener<M> {
            private lateinit var subscription: Closeable

            override fun setSubscription(subscription: Closeable) {
                this.subscription = subscription
            }

            override suspend fun willPut(model: M, typeName: String, metadata: Metadata, options: Array<out Options.Write>) {
                willPut?.invoke(WillPut(typeName, options, subscription), model)
            }

            override suspend fun didPut(model: M, getKey: suspend () -> Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) {
                didPut?.invoke(DidPut(getKey, typeName, options, subscription), model)
            }

            override suspend fun willDelete(key: Key<*>, getModel: () -> M?, typeName: String, options: Array<out Options.Write>) {
                if (didDeleteNeedsModel)
                    getModel()
                willDelete?.invoke(WillDelete(key, typeName, options, subscription), getModel)
            }

            override suspend fun didDelete(key: Key<*>, model: M?, typeName: String, options: Array<out Options.Write>) {
                didDelete?.invoke(DidDelete(key, typeName, options), model)
            }
        }

    }

}
