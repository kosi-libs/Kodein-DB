package org.kodein.db.impl.utils

import kotlin.reflect.KClass

public actual fun KClass<*>.kIsInstance(value: Any?): Boolean = java.isInstance(value)
