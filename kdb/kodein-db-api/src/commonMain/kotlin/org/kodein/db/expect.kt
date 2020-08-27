package org.kodein.db

import kotlin.reflect.KClass

public expect fun simpleTypeNameOf(type: KClass<*>): String

public fun simpleTypeAsciiNameOf(type: KClass<*>): ByteArray = simpleTypeNameOf(type).let { name -> ByteArray(name.length) { name[it].toByte() } }
