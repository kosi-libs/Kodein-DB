package org.kodein.db.model

import org.kodein.db.*
import kotlin.reflect.KClass

public interface ModelWrite : KeyMaker {
    public fun <M : Any> put(type: TKType<M>, model: M, vararg options: Options.Write): KeyAndSize<M>
    public fun <M : Any> put(type: TKType<M>, key: Key<M>, model: M, vararg options: Options.Write): Int

    public fun <M : Any> delete(type: TKType<M>, key: Key<M>, vararg options: Options.Write)
}

public inline fun <reified M : Any> ModelWrite.put(model: M, vararg options: Options.Write): KeyAndSize<M> = put(tTypeOf(), model, *options)
public inline fun <reified M : Any> ModelWrite.put(key: Key<M>, model: M, vararg options: Options.Write): Int = put(tTypeOf(), key, model, *options)
public inline fun <reified M : Any> ModelWrite.delete(key: Key<M>, vararg options: Options.Write): Unit = delete(tTypeOf(), key, *options)

public fun ModelWrite.putAll(models: Iterable<Any>, vararg options: Options.Write): Unit = models.forEach { put(it, *options) }

public operator fun ModelWrite.plusAssign(model: Any) { put(model) }
