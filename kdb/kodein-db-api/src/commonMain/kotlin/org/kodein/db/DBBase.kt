package org.kodein.db

import kotlin.reflect.KClass

interface DBBase {

    fun <M : Any> getHeapKey(type: KClass<M>, primaryKey: Value): Key<M>
    fun <M : Any> getNativeKey(type: KClass<M>, primaryKey: Value): Key.Native<M>

    fun <M : Any> getHeapKey(model: M, vararg options: Options.Write): Key<M>
    fun <M : Any> getNativeKey(model: M, vararg options: Options.Write): Key.Native<M>

}

inline fun <reified M : Any> DBBase.getHeapKey(primaryKey: Value) = getHeapKey(M::class, primaryKey)
inline fun <reified M : Any> DBBase.getNativeKey(primaryKey: Value) = getNativeKey(M::class, primaryKey)
