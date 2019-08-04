package org.kodein.db

interface Sized<out V> {
    val value: V
    val size: Int

    operator fun component1() = value
    operator fun component2() = size

    private data class Impl<V>(override val value: V, override val size: Int) : Sized<V>

    companion object {
        operator fun <V> invoke(value: V, size: Int): Sized<V> = Impl(value, size)
    }

}
