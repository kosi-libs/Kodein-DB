package org.kodein.db

import kotlin.reflect.KClass

public expect fun simpleTypeNameOf(type: KClass<*>): String
