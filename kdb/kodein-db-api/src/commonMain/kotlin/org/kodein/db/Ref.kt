package org.kodein.db

import org.kodein.db.model.ModelDB

interface Ref<out T : Any>

inline operator fun <reified T : Any> DB.get(ref: Ref<T>, vararg options: Options.Read) = get(getHeapKey(T::class, ref), *options)
inline operator fun <reified T : Any> ModelDB.get(ref: Ref<T>, vararg options: Options.Read) = get(getHeapKey(T::class, ref), *options)

fun <M : Any> DB.getRef(model: M, vararg options: Options.Write) = getRef(getHeapKey(model, *options))
fun <M : Any> ModelDB.getRef(model: M, vararg options: Options.Write) = getRef(getHeapKey(model, *options))
