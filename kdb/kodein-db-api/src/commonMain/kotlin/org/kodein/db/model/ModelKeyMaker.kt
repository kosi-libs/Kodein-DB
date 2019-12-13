package org.kodein.db.model

import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Value
import kotlin.reflect.KClass

interface ModelKeyMaker {

    fun <M : Any> newKey(type: KClass<M>, id: Value): Key<M>

    fun <M : Any> newKeyFrom(model: M, vararg options: Options.Write): Key<M>
}

inline fun <reified M : Any> ModelKeyMaker.newKey(id: Value) = newKey(M::class, id)
