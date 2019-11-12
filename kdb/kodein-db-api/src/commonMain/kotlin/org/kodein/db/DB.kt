package org.kodein.db

import org.kodein.memory.Closeable
import org.kodein.memory.use
import kotlin.reflect.KClass

interface DB : DBRead, DBWrite, Closeable {

    fun newBatch(): Batch

    fun newSnapshot(vararg options: Options.Read): Snapshot

    interface RegisterDsl<M : Any> {
        fun filter(f: (M) -> Boolean): RegisterDsl<M>
        fun register(listener: DBListener<M>): Closeable
        fun register(builder: DBListener.Builder<M>.() -> Unit): Closeable
    }

    fun onAll(): RegisterDsl<Any>

    fun <M : Any> on(type: KClass<M>): RegisterDsl<M>

    companion object
}

inline fun DB.execBatch(vararg options: Options.Write, block: Batch.() -> Unit) =
        newBatch().use {
            it.block()
            it.write(*options)
        }

inline fun <R> DB.useSnaphost(vararg options: Options.Read, block: (Snapshot) -> R) = newSnapshot(*options).use(block)

inline fun <reified M : Any> DB.on() = on(M::class)
