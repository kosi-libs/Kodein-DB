package org.kodein.db

import kotlin.reflect.KClass

public actual fun simpleTypeNameOf(type: KClass<*>): String = type.java.simpleName
