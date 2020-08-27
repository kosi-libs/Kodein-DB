package org.kodein.db

import kotlin.reflect.KClass

public actual fun simpleTypeNameOf(type: KClass<*>): String = type.simpleName ?: throw IllegalStateException("Could not find simple name of type")
