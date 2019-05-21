package org.kodein.db

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface Options {

    interface Read : Options

    interface Write : Options

    interface Open : Options
}

inline fun <O : Options, reified T : O> Array<out O>.get() = firstOrNull { it is T } as T?
