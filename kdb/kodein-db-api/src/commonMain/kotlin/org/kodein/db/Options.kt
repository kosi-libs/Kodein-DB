package org.kodein.db

interface Options {

    interface Read : Options

    interface Write : Options

    interface Open : Options
}

inline operator fun <reified T : Options> Array<out Options>.invoke() = firstOrNull { it is T } as T?
inline fun <reified T : Options> Array<out Options>.all() = filterIsInstance<T>()

@Suppress("UNCHECKED_CAST")
fun <T : Options> Array<out T>.and(add: T) = (this as Array<T>) + add
