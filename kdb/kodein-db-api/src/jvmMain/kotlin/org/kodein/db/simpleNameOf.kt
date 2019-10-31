package org.kodein.db

import kotlin.reflect.KClass

actual fun simpleTypeNameOf(type: KClass<*>): String = type.java.simpleName
