package org.kodein.db

import kotlin.reflect.KClass

interface KeyMaker {

    fun <M : Any> newKey(type: KClass<M>, id: Value): Key<M>

    fun <M : Any> newKey(model: M, vararg options: Options.Write): Key<M>
}

inline fun <reified M : Any> KeyMaker.newKey(id: Value) = newKey(M::class, id)
