package org.kodein.db

import kotlin.reflect.KClass

interface DBWrite : KeyMaker {
    fun <M : Any> put(model: M, vararg options: Options.Write): Key<M>
    fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Write)

    fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write)
}

inline fun <reified M : Any> DBWrite.delete(key: Key<M>, vararg options: Options.Write) = delete(M::class, key, *options)
