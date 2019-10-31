package org.kodein.db

import kotlin.reflect.KClass

interface KeyMaker {

    fun <M : Any> newHeapKey(type: KClass<M>, id: Value): Key<M>
    fun <M : Any> newNativeKey(type: KClass<M>, id: Value): Key.Native<M>

    fun <M : Any> newHeapKey(model: M, vararg options: Options.Write): Key<M>
    fun <M : Any> newNativeKey(model: M, vararg options: Options.Write): Key.Native<M>
}

inline fun <reified M : Any> KeyMaker.newHeapKey(id: Value) = newHeapKey(M::class, id)
inline fun <reified M : Any> KeyMaker.newNativeKey(id: Value) = newNativeKey(M::class, id)
