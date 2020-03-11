package org.kodein.db

import kotlin.reflect.KClass

interface KeyMaker {

    fun <M : Any> newKey(type: KClass<M>, vararg id: Any): Key<M>

    fun <M : Any> newKeyFrom(model: M, vararg options: Options.Write): Key<M>

    fun <M : Any> newKeyFromB64(type: KClass<M>, b64: String): Key<M>
}

inline fun <reified M : Any> KeyMaker.newKey(vararg id: Any) = newKey(M::class, *id)
inline fun <reified M : Any> KeyMaker.newKeyFromB64(b64: String) = newKeyFromB64(M::class, b64)
