package org.kodein.db.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import org.kodein.db.*
import org.kodein.db.model.ModelBatch
import org.kodein.db.model.ModelDB
import org.kodein.db.model.ModelDBListener
import org.kodein.db.model.ModelWrite
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory
import kotlin.collections.all

internal class RegisterDslImpl<M : Any>(private val mdb: ModelDB, private val filters: List<(M) -> Boolean>) : DB.RegisterDsl<M> {

    class FilteredListener<M : Any>(private val listener: DBListener<M>, private val filters: List<(M) -> Boolean>) : DBListener<M> {
        override fun setSubscription(subscription: Closeable)  = listener.setSubscription(subscription)

        override fun willPut(operation: Operation.Put<M>) {
            if (filters.all { it(operation.model) }) listener.willPut(operation)
        }

        override fun didPut(operation: Operation.Put<M>) {
            if (filters.all { it(operation.model) }) listener.didPut(operation)
        }

        override fun willDelete(operation: Operation.Delete<M>) {
            if (filters.all { it(operation.model() ?: return) }) listener.willDelete(operation)
        }

        override fun didDelete(operation: Operation.Delete<M>) {
            if (filters.all { it(operation.model() ?: return) }) listener.didDelete(operation)
        }
    }

    class DB2MDBListener<M : Any>(private val listener: DBListener<M>) : ModelDBListener<M> {
        override fun setSubscription(subscription: Closeable) { listener.setSubscription(subscription) }

        override fun willPut(model: M, key: Key<M>, typeName: ReadMemory, metadata: Metadata, options: Array<out Options.Puts>) {
            listener.willPut(Operation.Put(options.all(), key, model))
        }

        override fun didPut(model: M, key: Key<M>, typeName: ReadMemory, metadata: Metadata, size: Int, options: Array<out Options.Puts>) {
            listener.didPut(Operation.Put(options.all(), key, model))
        }

        override fun willDelete(key: Key<M>, getModel: () -> M?, typeName: ReadMemory, options: Array<out Options.Deletes>) {
            listener.willDelete(Operation.Delete(options.all(), key, getModel))
        }

        override fun didDelete(key: Key<M>, model: M?, typeName: ReadMemory, options: Array<out Options.Deletes>) {
            listener.didDelete(Operation.Delete(options.all(), key) { model })
        }
    }

    @Suppress("OverridingDeprecatedMember")
    override fun filter(f: (M) -> Boolean): DB.RegisterDsl<M> = RegisterDslImpl(mdb, filters + f)

    private fun wrap(listener: DBListener<M>): ModelDBListener<M> {
        if (filters.isEmpty())
            return DB2MDBListener(listener)
        return DB2MDBListener(FilteredListener(listener, filters))
    }

    @Suppress("UNCHECKED_CAST")
    override fun register(listener: DBListener<M>): Closeable = mdb.register(wrap(listener) as ModelDBListener<Any>)

    @Suppress("UNCHECKED_CAST")
    override fun register(builder: DBListener.Builder<M>.() -> Unit): Closeable = mdb.register(wrap(DBListener.Builder<M>().apply(builder).build()) as ModelDBListener<Any>)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun putFlow(): Flow<Operation.Put<M>> = callbackFlow {
        val subscription = register(object : DBListener<M> {
            override fun didPut(operation: Operation.Put<M>) { trySend(operation) }
        })
        awaitClose { subscription.close() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun deleteFlow(): Flow<Operation.Delete<M>> = callbackFlow {
        val subscription = register(object : DBListener<M> {
            override fun willDelete(operation: Operation.Delete<M>) { operation.model() }
            override fun didDelete(operation: Operation.Delete<M>) { trySend(operation) }
        })
        awaitClose { subscription.close() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun operationFlow(): Flow<Operation<M>> = callbackFlow {
        val subscription = register(object : DBListener<M> {
            override fun didPut(operation: Operation.Put<M>) { trySend(operation) }
            override fun willDelete(operation: Operation.Delete<M>) { operation.model() }
            override fun didDelete(operation: Operation.Delete<M>) { trySend(operation) }
        })
        awaitClose { subscription.close() }
    }
}
