package org.kodein.db

import kotlin.reflect.KClass

expect fun simpleTypeNameOf(type: KClass<*>): String
