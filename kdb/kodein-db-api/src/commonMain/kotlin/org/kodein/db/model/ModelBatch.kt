package org.kodein.db.model

import org.kodein.db.Key
import org.kodein.db.KeyAndSize
import org.kodein.db.Options
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable
import kotlin.reflect.KClass

public interface ModelBatch : ModelWrite, Closeable {
    override fun <M : Any> put(model: M, vararg options: Options.Puts): KeyAndSize<M> = put(model, *(options as Array<out Options.BatchPut>))
    public fun <M : Any> put(model: M, vararg options: Options.BatchPut): KeyAndSize<M>

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Puts): Int = put(key, model, *(options as Array<out Options.BatchPut>))
    public fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.BatchPut): Int

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Deletes) { delete(type, key, *(options as Array<out Options.BatchDelete>)) }
    public fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.BatchDelete)

    public fun write(afterErrors: MaybeThrowable, vararg options: Options.BatchWrite)
}

public inline fun <reified M : Any> ModelBatch.delete(key: Key<M>, vararg options: Options.BatchDelete): Unit = delete(M::class, key, *options)

public fun ModelBatch.putAll(models: Iterable<Any>, vararg options: Options.BatchPut): Unit = models.forEach { put(it, *options) }
