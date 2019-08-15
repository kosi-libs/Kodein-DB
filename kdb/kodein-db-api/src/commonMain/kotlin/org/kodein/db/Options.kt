package org.kodein.db

interface Options {

    interface Read : Options

    interface Write : Options

    interface Open : Options
}

inline operator fun <reified T : Options> Array<out Options>.invoke() = firstOrNull { it is T } as T?
inline fun <reified T : Options> Array<out Options>.all() = filterIsInstance<T>()
