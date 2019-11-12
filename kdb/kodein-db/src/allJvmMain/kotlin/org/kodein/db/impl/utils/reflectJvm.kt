package org.kodein.db.impl.utils

import kotlin.reflect.KClass

actual fun KClass<*>.kIsInstance(value: Any?) = java.isInstance(value)
