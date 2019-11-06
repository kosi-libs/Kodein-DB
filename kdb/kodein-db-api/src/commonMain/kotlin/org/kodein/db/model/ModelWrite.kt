package org.kodein.db.model

import org.kodein.db.*
import kotlin.reflect.KClass

interface ModelWrite : KeyMaker {
    fun <M : Any> put(model: M, vararg options: Options.Write): KeyAndSize<M>
    fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Write): Int

    fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write)
}

inline fun <reified M : Any> ModelWrite.delete(key: Key<M>, vararg options: Options.Write) = delete(M::class, key, *options)

fun ModelWrite.putAll(models: Iterable<Any>, vararg options: Options.Write) = models.forEach { put<Any>(it, *options) }

operator fun ModelWrite.plusAssign(model: Any) { put<Any>(model) }
