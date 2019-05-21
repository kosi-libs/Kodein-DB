package org.kodein.db

import kotlin.reflect.KClass

actual fun simpleNameOf(type: KClass<*>): String = type.simpleName ?: throw IllegalStateException("Could not find simple name of type")
