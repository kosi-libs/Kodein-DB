package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.model.orm.Serializer
import kotlin.reflect.KClass

public class DBClassSerializer<M : Any>(public val cls: KClass<M>, public val serializer: Serializer<M>) : Options.Open
public inline operator fun <reified M : Any> Serializer<M>.unaryPlus(): DBClassSerializer<M> = DBClassSerializer(M::class, this)
