package org.kodein.db

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.kodein.memory.Closeable
import org.kodein.memory.use
import kotlin.reflect.KClass

public interface DB : DBRead, DBWrite, Closeable {

    override fun <M : Any> put(model: M, vararg options: Options.Puts): Key<M> = put(model, *(options as Array<out Options.DirectPut>))
    public fun <M : Any> put(model: M, vararg options: Options.DirectPut): Key<M>

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Puts) { put(key, model, *(options as Array<out Options.DirectPut>)) }
    public fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.DirectPut)

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Deletes) { delete(type, key, *(options as Array<out Options.DirectDelete>)) }
    public fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.DirectDelete)

    public fun newBatch(vararg options: Options.NewBatch): Batch

    public fun newSnapshot(vararg options: Options.NewSnapshot): Snapshot

    public interface RegisterDsl<M : Any> {
        // Deprecated since 0.9.0
        @Deprecated("Use the flow API for sequence-like operations.")
        public fun filter(f: (M) -> Boolean): RegisterDsl<M>
        public fun register(listener: DBListener<M>): Closeable
        public fun register(builder: DBListener.Builder<M>.() -> Unit): Closeable

        public fun putFlow(): Flow<Operation.Put<M>>
        public fun deleteFlow(): Flow<Operation.Delete<M>>
        public fun operationFlow(): Flow<Operation<M>>
    }

    public fun onAll(): RegisterDsl<Any>
    public fun <M : Any> on(type: KClass<M>): RegisterDsl<M>

    public fun <M : Any> flowOf(type: KClass<M>, key: Key<M>, init: Boolean = true): Flow<M?>

    public companion object
}

public inline fun <reified M : Any> DB.delete(key: Key<M>, vararg options: Options.DirectDelete): Unit = delete(M::class, key, *options)

public inline fun <reified M : Any> DB.deleteById(vararg id: Any, options: Array<out Options.DirectDelete> = emptyArray()): Unit = delete(keyById(*id), *options)

public inline fun <reified M : Any> DB.deleteAll(cursor: Cursor<M>, vararg options: Options.DirectDelete) {
    cursor.useKeys { seq -> seq.forEach { delete(it, *options) } }
}

public class ExecBatch(private val batch: Batch) : Batch by batch {
    internal val options = ArrayList<Options.BatchWrite>()

    public fun addOptions(vararg options: Options.BatchWrite) { this.options.addAll(options) }
    public fun Options.BatchWrite.unaryPlus(): Unit = addOptions(this)

    override fun write(vararg options: Options.BatchWrite) {
        batch.write(*(this.options + options).toTypedArray())
    }
}

public inline fun DB.execBatch(vararg options: Options.BatchWrite, block: ExecBatch.() -> Unit): Unit =
    ExecBatch(newBatch()).use {
        it.block()
        it.write(*options)
    }

public inline fun <reified M : Any> DB.on(): DB.RegisterDsl<M> = on(M::class)

public inline fun <reified M : Any> DB.flowOf(key: Key<M>, init: Boolean = true): Flow<M?> = flowOf(M::class, key, init)

public suspend fun <M : Any> DB.stateFlowOf(scope: CoroutineScope, type: KClass<M>, key: Key<M>): StateFlow<M?> =
    flowOf(type, key, true).stateIn(scope)

public suspend inline fun < reified M : Any> DB.stateFlowOf(scope: CoroutineScope, key: Key<M>): StateFlow<M?> =
    stateFlowOf(scope, M::class, key)
public suspend inline fun < reified M : Any> DB.stateFlowOfId(scope: CoroutineScope, vararg id: Any): StateFlow<M?> =
    stateFlowOf(scope, M::class, keyById(*id))

public suspend inline fun < reified M : Any> DB.stateFlowFrom(scope: CoroutineScope, model: M): StateFlow<M?> =
    stateFlowOf(scope, M::class, keyFrom(model))
