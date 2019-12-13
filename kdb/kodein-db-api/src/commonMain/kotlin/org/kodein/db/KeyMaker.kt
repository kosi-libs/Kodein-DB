package org.kodein.db

import kotlin.reflect.KClass

interface KeyMaker {

    fun <M : Any> newKey(type: KClass<M>, vararg id: Any): Key<M>

    fun <M : Any> newKeyFrom(model: M, vararg options: Options.Write): Key<M>
}

inline fun <reified M : Any> KeyMaker.newKey(vararg id: Any) = newKey(M::class, *id)
