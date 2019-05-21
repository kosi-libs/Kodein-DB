package org.kodein.db.model

import org.kodein.db.Options

interface ModelWrite : ModelBase {

    fun put(model: Any, vararg options: Options.Write)

    fun <M : Any> putAndGetKey(model: M, vararg options: Options.Write): Key<M>

    fun delete(key: Key<*>, vararg options: Options.Write)
}

fun ModelWrite.putAll(models: Iterable<Any>, vararg options: Options.Write) = models.forEach { put(it, *options) }

operator fun ModelWrite.plusAssign(model: Any) = put(model)
