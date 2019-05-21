package org.kodein.db

import kotlin.reflect.KClass

expect fun simpleNameOf(type: KClass<*>): String

