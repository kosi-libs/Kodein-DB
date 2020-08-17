package org.kodein.db

import org.kodein.db.model.orm.Metadata
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory

public interface DBListener<in M : Any> {

    public fun setSubscription(subscription: Closeable) {}

    public fun willPut(model: M, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Write>) {}

    public fun didPut(model: M, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Write>) {}

    public fun willDelete(key: Key<*>, getModel: () -> M?, typeName: ReadMemory, options: Array<out Options.Write>) {}

    public fun didDelete(key: Key<*>, model: M?, typeName: ReadMemory, options: Array<out Options.Write>) {}

    public class Builder<M : Any> {
        public class WillPut(public val typeName: ReadMemory, public val options: Array<out Options.Write>, public val subscription: Closeable)

        public class DidPut(public val key: Key<*>, public val typeName: ReadMemory, public val options: Array<out Options.Write>, public val subscription: Closeable)

        public class WillDelete(public val key: Key<*>, public val typeName: ReadMemory, public val options: Array<out Options.Write>, public val subscription: Closeable)

        public class DidDelete(public val key: Key<*>, public val typeName: ReadMemory, public val options: Array<out Options.Write>, public val subscription: Closeable)

        private var willPut: (WillPut.(M) -> Unit)? = null
        private var didPut: (DidPut.(M) -> Unit)? = null

        private var willDelete: (WillDelete.(() -> M?) -> Unit)? = null
        private var didDelete: (DidDelete.(M?) -> Unit)? = null

        private var didDeleteNeedsModel = false

        public fun willPut(block: WillPut.(M) -> Unit) {
            willPut = block
        }

        public fun didPut(block: DidPut.(M) -> Unit) {
            didPut = block
        }

        public fun willDelete(block: WillDelete.() -> Unit) {
            willDelete = { block() }
        }

        public fun willDeleteIt(block: WillDelete.(M) -> Unit) {
            willDelete = { it()?.let { block(it) } }
        }

        public fun didDelete(block: DidDelete.() -> Unit) {
            didDelete = { block() }
        }

        public fun didDeleteIt(block: DidDelete.(M) -> Unit) {
            didDeleteNeedsModel = true
            didDelete = { it?.let { block(it) } }
        }

        public fun build(): DBListener<M> = object : DBListener<M> {
            private lateinit var subscription: Closeable

            override fun setSubscription(subscription: Closeable) {
                this.subscription = subscription
            }

            override fun willPut(model: M, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Write>) {
                willPut?.invoke(WillPut(typeName, options, subscription), model)
            }

            override fun didPut(model: M, key: Key<*>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Write>) {
                didPut?.invoke(DidPut(key, typeName, options, subscription), model)
            }

            override fun willDelete(key: Key<*>, getModel: () -> M?, typeName: ReadMemory, options: Array<out Options.Write>) {
                if (didDeleteNeedsModel)
                    getModel()
                willDelete?.invoke(WillDelete(key, typeName, options, subscription), getModel)
            }

            override fun didDelete(key: Key<*>, model: M?, typeName: ReadMemory, options: Array<out Options.Write>) {
                didDelete?.invoke(DidDelete(key, typeName, options, subscription), model)
            }
        }

    }

}
