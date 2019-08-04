package org.kodein.db

import org.kodein.db.data.DataDB
import org.kodein.db.model.ModelDB
import org.kodein.memory.Closeable
import org.kodein.memory.use
import kotlin.reflect.KClass

interface DB : DBRead, DBWrite, Closeable {

//    class OpenOptions(
//            val serializer: Serializer<Any>? = null,
//            val metadataExtractor: MetadataExtractor? = null,
//            val typeTable: TypeTable? = null
//    ) : Options.Open

    interface Factory {
        fun disableCache()
        fun addModelMiddleware(middleware: (ModelDB) -> ModelDB)
        fun addDataMiddleware(middleware: (DataDB) -> DataDB)
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
