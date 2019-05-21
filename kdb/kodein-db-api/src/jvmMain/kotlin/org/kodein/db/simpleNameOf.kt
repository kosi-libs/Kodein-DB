package org.kodein.db

import kotlin.reflect.KClass

actual fun simpleNameOf(type: KClass<*>): String = type.java.simpleName
