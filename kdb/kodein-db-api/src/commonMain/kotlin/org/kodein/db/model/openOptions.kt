package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.model.orm.Serializer
import kotlin.reflect.KClass

class DBClassSerializer<M : Any>(val cls: KClass<M>, val serializer: Serializer<M>) : Options.Open
inline operator fun <reified M : Any> Serializer<M>.unaryPlus() = DBClassSerializer(M::class, this)
