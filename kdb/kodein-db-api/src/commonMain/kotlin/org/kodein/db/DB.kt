package org.kodein.db

import org.kodein.memory.Closeable
import org.kodein.memory.use
import kotlin.reflect.KClass

interface DB : DBRead, DBWrite, Closeable {

    fun newBatch(): DBBatch

    fun newSnapshot(vararg options: Options.Read): DBSnapshot

    interface RegisterDsl<M : Any> {
        fun filter(f: (M) -> Boolean): RegisterDsl<M>
        fun register(listener: DBListener<M>): Closeable
        fun register(builder: DBListener.Builder<M>.() -> Unit): Closeable
    }

    fun onAll(): RegisterDsl<Any>

    fun <M : Any> on(type: KClass<M>): RegisterDsl<M>

//    interface Box<T : Any> {
//        fun get(): T?
//        fun set(value: T)
//        fun update(transform: (T?) -> T?)
//        fun delete()
//    }
//
//    fun <T : Any> box(model: T): Box<T>

    companion object
}

inline fun DB.execBatch(vararg options: Options.Write, block: (DBBatch) -> Unit) =
        newBatch().use {
            block(it)
            it.write(*options)
        }

inline fun <R> DB.useSnaphost(vararg options: Options.Read, block: (DBSnapshot) -> R) = newSnapshot(*options).use(block)

inline fun <reified M : Any> DB.on() = on(M::class)
