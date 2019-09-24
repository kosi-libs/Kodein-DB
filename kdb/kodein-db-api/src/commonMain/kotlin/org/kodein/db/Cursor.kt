package org.kodein.db

import org.kodein.memory.use

interface Cursor<M: Any> : BaseCursor {

    fun transientKey(): TransientKey<M>
    fun model(vararg options: Options.Read): M

    fun nextEntries(size: Int): Entries<M>

    interface Entries<M: Any> : BaseCursor.BaseEntries {
        fun key(i: Int): Key<M>
        operator fun get(i: Int, vararg options: Options.Read): M
    }

}

fun <M : Any> Cursor<M>.models(): Sequence<M> = sequence {
    use {
        while (isValid())
            yield(model())
    }
}

fun <M : Any> Cursor.Entries<M>.models(): Iterable<M> = object : Iterable<M> {
    override fun iterator(): Iterator<M> = iterator {
        for (i in 0 until size)
            yield(get(i))
    }
}
