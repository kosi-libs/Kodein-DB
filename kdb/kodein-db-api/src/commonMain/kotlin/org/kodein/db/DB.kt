package org.kodein.db

import org.kodein.memory.Closeable
import org.kodein.memory.use
import kotlin.reflect.KClass

interface DB : DBRead, DBWrite, Closeable {

    interface Factory {
        fun open(path: String, vararg options: Options.Open): DB
        fun destroy(path: String, vararg options: Options.Open)
    }

    interface Batch : DBWrite, Closeable {
        fun write(vararg options: Options.Write)
    }

    fun newBatch(): Batch

    interface Snapshot : DBRead, Closeable

    fun newSnapshot(vararg options: Options.Read): Snapshot

    interface RegisterDsl<M : Any> {
        fun filter(f: (M) -> Boolean): RegisterDsl<M>
        fun register(listener: DBListener<M>): Closeable
        fun register(builder: DBListener.Builder<M>.() -> Unit): Closeable
    }

    fun onAll(): RegisterDsl<Any>

    fun <M : Any> on(type: KClass<M>): RegisterDsl<M>
}

inline fun DB.execBatch(vararg options: Options.Write, block: (DB.Batch) -> Unit) =
        newBatch().use {
            block(it)
            it.write(*options)
        }

inline fun <R> DB.useSnaphost(vararg options: Options.Read, block: (DB.Snapshot) -> R) = newSnapshot(*options).use(block)

inline fun <reified M : Any> DB.on() = on(M::class)
