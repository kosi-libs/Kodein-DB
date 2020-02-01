package org.kodein.db

import kotlin.reflect.KClass

expect fun simpleTypeNameOf(type: KClass<*>): String

fun simpleTypeAsciiNameOf(type: KClass<*>) = simpleTypeNameOf(type).let { name -> ByteArray(name.length) { name[it].toByte() } }
