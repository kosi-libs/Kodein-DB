package org.kodein.db.impl.utils

import kotlin.reflect.KClass

expect fun KClass<*>.kIsInstance(value: Any?): Boolean
