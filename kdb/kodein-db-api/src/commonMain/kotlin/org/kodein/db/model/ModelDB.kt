package org.kodein.db.model

import org.kodein.db.*
import org.kodein.db.data.DataDB
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory
import kotlin.reflect.KClass

public interface ModelDB : ModelWrite, ModelRead, ModelTypeMatcher, Closeable {

    override fun <M : Any> put(model: M, vararg options: Options.Puts): KeyAndSize<M> = put(model, *(options as Array<out Options.DirectPut>))
    public fun <M : Any> put(model: M, vararg options: Options.DirectPut): KeyAndSize<M>

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Puts): Int = put(key, model, *(options as Array<out Options.DirectPut>))
    public fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.DirectPut): Int

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Deletes) { delete(type, key, *(options as Array<out Options.DirectDelete>)) }
    public fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.DirectDelete)

    public fun newBatch(vararg options: Options.NewBatch): ModelBatch

    public fun newSnapshot(vararg options: Options.NewSnapshot): ModelSnapshot

    public fun register(listener: ModelDBListener<Any>): Closeable

    public fun <T: Any> getExtension(key: ExtensionKey<T>): T?

    public val data: DataDB

    public companion object
}

public inline fun <reified M : Any> ModelDB.delete(key: Key<M>, vararg options: Options.DirectDelete): Unit = delete(M::class, key, *options)

public fun ModelDB.putAll(models: Iterable<Any>, vararg options: Options.DirectPut): Unit = models.forEach { put(it, *options) }
