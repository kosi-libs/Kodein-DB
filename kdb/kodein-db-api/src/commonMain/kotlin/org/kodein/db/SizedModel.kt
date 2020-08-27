package org.kodein.db

public interface Sized<out M> {
    public val model: M
    public val size: Int

    public operator fun component1(): M = model
    public operator fun component2(): Int = size

    private data class Impl<M>(override val model: M, override val size: Int) : Sized<M>

    public companion object {
        public operator fun <M> invoke(value: M, size: Int): Sized<M> = Impl(value, size)
    }

}

public data class KeyAndSize<M : Any>(val key: Key<M>, val size: Int)
