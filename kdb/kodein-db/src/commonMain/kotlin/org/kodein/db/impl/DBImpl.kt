package org.kodein.db.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.kodein.db.*
import org.kodein.db.impl.utils.kIsInstance
import org.kodein.db.model.ModelDB
import org.kodein.memory.Closeable
import kotlin.reflect.KClass

internal class DBImpl(override val mdb: ModelDB) : DB, DBReadModule, KeyMaker by mdb, Closeable by mdb {

    override fun <M : Any> put(model: M, vararg options: Options.DirectPut): Key<M> =
        mdb.put(model, *options).key

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.DirectPut) {
        mdb.put(key, model, *options)
    }

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.DirectDelete) {
        mdb.delete(type, key, *options)
    }

    override fun newBatch(vararg options: Options.NewBatch): Batch = BatchImpl(mdb.newBatch())

    override fun newSnapshot(vararg options: Options.NewSnapshot): Snapshot = SnapshotImpl(mdb.newSnapshot(*options))

    override fun onAll(): DB.RegisterDsl<Any> = RegisterDslImpl(mdb, emptyList())

    override fun <M : Any> on(type: KClass<M>): DB.RegisterDsl<M> = RegisterDslImpl(mdb, listOf<(M) -> Boolean>({ type.kIsInstance(it) }))

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun <M : Any> flowOf(type: KClass<M>, key: Key<M>, init: Boolean): Flow<M?> = callbackFlow {
        val subscription = on(type).register(object : DBListener<M> {
            override fun didDelete(operation: Operation.Delete<M>) { trySend(null) }
            override fun didPut(operation: Operation.Put<M>) { trySend(operation.model) }
        })
        if (init) trySend(get(type, key))
        awaitClose { subscription.close() }
    }
}
