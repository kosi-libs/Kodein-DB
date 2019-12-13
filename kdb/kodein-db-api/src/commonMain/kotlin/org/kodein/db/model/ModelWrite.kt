package org.kodein.db.model

import org.kodein.db.Key
import org.kodein.db.KeyAndSize
import org.kodein.db.KeyMaker
import org.kodein.db.Options
import kotlin.reflect.KClass

interface ModelWrite : ModelKeyMaker {
    fun <M : Any> put(model: M, vararg options: Options.Write): KeyAndSize<M>
    fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Write): Int

    fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write)
}

inline fun <reified M : Any> ModelWrite.delete(key: Key<M>, vararg options: Options.Write) = delete(M::class, key, *options)

fun ModelWrite.putAll(models: Iterable<Any>, vararg options: Options.Write) = models.forEach { put(it, *options) }

operator fun ModelWrite.plusAssign(model: Any) { put(model) }
