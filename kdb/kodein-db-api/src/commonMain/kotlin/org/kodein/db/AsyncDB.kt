package org.kodein.db

import org.kodein.memory.Closeable
import org.kodein.memory.use
import kotlin.reflect.KClass

interface AsyncDB : AsyncDBRead, AsyncDBWrite, Closeable {

    suspend fun newBatch(): AsyncBatch

    suspend fun newSnapshot(vararg options: Options.Read): AsyncSnapshot

    interface RegisterDsl<M : Any> {
        fun filter(f: suspend (M) -> Boolean): RegisterDsl<M>
        fun register(listener: AsyncDBListener<M>): Closeable
        fun register(builder: AsyncDBListener.Builder<M>.() -> Unit): Closeable
    }

    fun onAll(): RegisterDsl<Any>

    fun <M : Any> on(type: KClass<M>): RegisterDsl<M>

    interface Box<T : Any> {
        val key: Key<T>
        suspend fun unbox(block: suspend (T?) -> Unit)
        suspend fun update(vararg options: Options.Write, transform: suspend (T?) -> T?)
    }

    fun <M : Any> box(model: M): Box<M>

    fun <M : Any> box(type: KClass<M>, key: Key<M>)

    override fun sync(): DB

    companion object
}

suspend inline fun AsyncDB.execBatch(vararg options: Options.Write, block: (AsyncBatch) -> Unit) =
        newBatch().use {
            block(it)
            it.write(*options)
        }

suspend inline fun <R> AsyncDB.useSnaphost(vararg options: Options.Read, block: (AsyncSnapshot) -> R) = newSnapshot(*options).use(block)

inline fun <reified M : Any> AsyncDB.on() = on(M::class)
