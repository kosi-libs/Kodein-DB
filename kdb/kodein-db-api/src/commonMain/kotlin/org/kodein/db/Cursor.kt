package org.kodein.db

import org.kodein.memory.use

interface Cursor<M: Any> : BaseCursor {

    fun key(): Key<M>
    fun model(vararg options: Options.Read): M

}

fun <M : Any> Cursor<M>.models(): Sequence<M> = sequence {
    use {
        while (isValid()) {
            yield(model())
            next()
        }
    }
}

data class Entry<M : Any>(val key: Key<M>, val model: M)

fun <M : Any> Cursor<M>.entries(): Sequence<Entry<M>> = sequence {
    use {
        while (isValid()) {
            yield(Entry(key(), model()))
            next()
        }
    }
}
