package org.kodein.db.impl.utils

import kotlin.reflect.KClass

public expect fun KClass<*>.kIsInstance(value: Any?): Boolean
