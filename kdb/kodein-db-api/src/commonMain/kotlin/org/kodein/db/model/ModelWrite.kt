package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.memory.model.Sized

interface ModelWrite : ModelBase {

    fun put(model: Any, vararg options: Options.Write): Int

    fun <M : Any> putAndGetHeapKey(model: M, vararg options: Options.Write): Sized<Key<M>>
    fun <M : Any> putAndGetNativeKey(model: M, vararg options: Options.Write): Sized<Key.Native<M>>

    fun delete(key: Key<*>, vararg options: Options.Write)
}

fun ModelWrite.putAll(models: Iterable<Any>, vararg options: Options.Write) = models.forEach { put(it, *options) }

operator fun ModelWrite.plusAssign(model: Any) { put(model) }
