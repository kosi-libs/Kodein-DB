package org.kodein.db
import kotlin.collections.plus as kotlinPlus

public interface Options {

    public interface Read : Options

    public interface Write : Options

    public interface Open : Options
}

public inline operator fun <reified T : Options> Array<out Options>.invoke(): T? = firstOrNull { it is T } as T?
public inline fun <reified T : Options> Array<out Options>.all(): List<T> = filterIsInstance<T>()

@Suppress("UNCHECKED_CAST")
//fun <T : Options> Array<out T>.and(add: T) = (this as Array<T>) + add

public operator fun <T : Options> Array<out T>.plus(add: T): Array<T> = (this as Array<T>).kotlinPlus(add)
