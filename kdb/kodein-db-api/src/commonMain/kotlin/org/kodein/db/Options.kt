package org.kodein.db
import kotlin.collections.plus as kotlinPlus

interface Options {

    interface Read : Options

    interface Write : Options

    interface Open : Options
}

inline operator fun <reified T : Options> Array<out Options>.invoke() = firstOrNull { it is T } as T?
inline fun <reified T : Options> Array<out Options>.all() = filterIsInstance<T>()

@Suppress("UNCHECKED_CAST")
//fun <T : Options> Array<out T>.and(add: T) = (this as Array<T>) + add

operator fun <T : Options> Array<out T>.plus(add: T) = (this as Array<T>).kotlinPlus(add)
