package org.kodein.db.model

import org.kodein.db.*
import kotlin.reflect.KClass

public interface ModelWrite : KeyMaker, ValueMaker {
    public fun <M : Any> put(model: M): KeyAndSize<M> = put(model, *emptyArray())
    public fun <M : Any> put(model: M, vararg options: Options.Puts): KeyAndSize<M>

    public fun <M : Any> put(key: Key<M>, model: M): Int = put(key, model, *emptyArray())
    public fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Puts): Int

    public fun <M : Any> delete(type: KClass<M>, key: Key<M>) { delete(type, key, *emptyArray()) }
    public fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Deletes)
}

public inline fun <reified M : Any> ModelWrite.delete(key: Key<M>): Unit = delete(key, *emptyArray())
public inline fun <reified M : Any> ModelWrite.delete(key: Key<M>, vararg options: Options.Deletes): Unit = delete(M::class, key, *options)

public fun ModelWrite.putAll(models: Iterable<Any>): Unit = putAll(models, *emptyArray())
public fun ModelWrite.putAll(models: Iterable<Any>, vararg options: Options.Puts): Unit = models.forEach { put(it, *options) }

public operator fun ModelWrite.plusAssign(model: Any) { put(model) }
