package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.Value
import kotlin.reflect.KClass

interface ModelBase {

    fun <M : Any> getHeapKey(type: KClass<M>, primaryKey: Value): Key<M>
    fun <M : Any> getNativeKey(type: KClass<M>, primaryKey: Value): Key.Native<M>

    fun <M : Any> getHeapKey(model: M, vararg options: Options.Write): Key<M>
    fun <M : Any> getNativeKey(model: M, vararg options: Options.Write): Key.Native<M>

}

inline fun <reified M : Any> ModelBase.getHeapKey(primaryKey: Value) = getHeapKey(M::class, primaryKey)
inline fun <reified M : Any> ModelBase.getNativeKey(primaryKey: Value) = getNativeKey(M::class, primaryKey)
