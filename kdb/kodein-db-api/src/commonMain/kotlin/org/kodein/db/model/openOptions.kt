package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.model.orm.Serializer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public class DBTypeSerializer<M : Any>(public val type: KType, public val serializer: Serializer<M>) : Options.Open
@OptIn(ExperimentalStdlibApi::class)
public inline operator fun <reified M : Any> Serializer<M>.unaryPlus(): DBTypeSerializer<M> = DBTypeSerializer(typeOf<M>(), this)
