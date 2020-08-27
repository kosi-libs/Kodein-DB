package org.kodein.db

import kotlin.reflect.KClass
import kotlin.reflect.KType

public fun simpleTypeNameOf(type: KType): String = (type.classifier as? KClass<*>)?.simpleName
        ?: error("Could not find simple name of $type")

public fun simpleTypeAsciiNameOf(type: KType): ByteArray = simpleTypeNameOf(type).let { name -> ByteArray(name.length) { name[it].toByte() } }
