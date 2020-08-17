package org.kodein.db

import org.kodein.memory.Closeable
import org.kodein.memory.use
import kotlin.reflect.KClass

public interface DB : DBRead, DBWrite, Closeable {

    public fun newBatch(): Batch

    public fun newSnapshot(vararg options: Options.Read): Snapshot

    public interface RegisterDsl<M : Any> {
        public fun filter(f: (M) -> Boolean): RegisterDsl<M>
        public fun register(listener: DBListener<M>): Closeable
        public fun register(builder: DBListener.Builder<M>.() -> Unit): Closeable
    }

    public fun onAll(): RegisterDsl<Any>

    public fun <M : Any> on(type: KClass<M>): RegisterDsl<M>

    public companion object
}

public inline fun DB.execBatch(vararg options: Options.Write, block: Batch.() -> Unit): Unit =
        newBatch().use {
            it.block()
            it.write(*options)
        }

public inline fun <reified M : Any> DB.on(): DB.RegisterDsl<M> = on(M::class)
