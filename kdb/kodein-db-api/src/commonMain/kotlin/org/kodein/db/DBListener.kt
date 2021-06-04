package org.kodein.db

import org.kodein.memory.Closeable

public interface DBListener<M : Any> {

    public fun setSubscription(subscription: Closeable) {}

    public fun willPut(operation: Operation.Put<M>) {}

    public fun didPut(operation: Operation.Put<M>) {}

    public fun willDelete(operation: Operation.Delete<M>) {}

    public fun didDelete(operation: Operation.Delete<M>) {}

    public class Builder<M : Any> {
        public interface Receiver<M : Any, O : Operation<M>> {
            public val operation: O
            public val subscription: Closeable
        }

        public class Put<M : Any>(override val operation: Operation.Put<M>, override val subscription: Closeable) : Receiver<M, Operation.Put<M>>
        public class Delete<M : Any>(override val operation: Operation.Delete<M>, override val subscription: Closeable) : Receiver<M, Operation.Delete<M>>

        private var willPut: (Put<M>.(M) -> Unit)? = null
        private var didPut: (Put<M>.(M) -> Unit)? = null

        private var willDelete: (Delete<M>.(() -> M?) -> Unit)? = null
        private var didDelete: (Delete<M>.(M?) -> Unit)? = null

        private var didDeleteNeedsModel = false

        public fun willPut(block: Put<M>.(M) -> Unit) {
            willPut = block
        }

        public fun didPut(block: Put<M>.(M) -> Unit) {
            didPut = block
        }

        public fun willDelete(block: Delete<M>.() -> Unit) {
            willDelete = { block() }
        }

        public fun willDeleteIt(block: Delete<M>.(M) -> Unit) {
            willDelete = { it()?.let { block(it) } }
        }

        public fun didDelete(block: Delete<M>.() -> Unit) {
            didDelete = { block() }
        }

        public fun didDeleteIt(block: Delete<M>.(M) -> Unit) {
            didDeleteNeedsModel = true
            didDelete = { it?.let { block(it) } }
        }

        public fun build(): DBListener<M> = object : DBListener<M> {
            private lateinit var subscription: Closeable

            override fun setSubscription(subscription: Closeable) {
                this.subscription = subscription
            }

            override fun willPut(operation: Operation.Put<M>) {
                willPut?.invoke(Put(operation, subscription), operation.model)
            }

            override fun didPut(operation: Operation.Put<M>) {
                didPut?.invoke(Put(operation, subscription), operation.model)
            }

            override fun willDelete(operation: Operation.Delete<M>) {
                if (didDeleteNeedsModel)
                    operation.model()
                willDelete?.invoke(Delete(operation, subscription), operation::model)
            }

            override fun didDelete(operation: Operation.Delete<M>) {
                didDelete?.invoke(Delete(operation, subscription), operation.model())
            }
        }

    }

}
