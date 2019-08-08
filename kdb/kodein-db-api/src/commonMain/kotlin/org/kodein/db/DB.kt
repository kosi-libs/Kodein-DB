package org.kodein.db

import org.kodein.db.data.DataDB
import org.kodein.db.model.ModelDB
import org.kodein.memory.Closeable
import org.kodein.memory.use
import kotlin.reflect.KClass

typealias ModelMiddleware = ((ModelDB) -> ModelDB)
typealias DataMiddleware = ((DataDB) -> DataDB)

interface DB : DBRead, DBWrite, Closeable {

    interface Factory {
        fun disableCache()
        fun addModelMiddleware(middleware: ModelMiddleware)
        fun addDataMiddleware(middleware: DataMiddleware)
        fun addOption(option: Options.Open)
    }

    interface Batch : DBWrite, Closeable {
        fun write(vararg options: Options.Write)
    }

    fun newBatch(): Batch

    interface Snapshot : DBRead, Closeable

    fun newSnapshot(vararg options: Options.Read): Snapshot

    interface RegisterDsl<M : Any> {
        fun filter(f: (M) -> Boolean): RegisterDsl<M>
        fun register(listener: DBListener): Closeable
        fun register(builder: DBListener.Builder.() -> Unit): Closeable
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
