package org.kodein.db

interface Sized<out M> {
    val model: M
    val size: Int

    operator fun component1() = model
    operator fun component2() = size

    private data class Impl<M>(override val model: M, override val size: Int) : Sized<M>

    companion object {
        operator fun <M> invoke(value: M, size: Int): Sized<M> = Impl(value, size)
    }

}

data class KeyAndSize<M : Any>(val key: Key<M>, val size: Int)
