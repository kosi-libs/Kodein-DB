package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.Value
import kotlin.reflect.KClass

interface ModelBase {

    fun <M : Any> getKey(type: KClass<M>, primaryKey: Value): Key<M>

    fun <M : Any> getKey(model: M, vararg options: Options.Write): Key<M>

}

inline fun <reified M : Any> ModelBase.getKey(primaryKey: Value) = getKey(M::class, primaryKey)
