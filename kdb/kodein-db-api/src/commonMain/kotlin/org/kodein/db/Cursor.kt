package org.kodein.db

import org.kodein.memory.use

public interface Cursor<M: Any> : BaseCursor {

    public fun key(): Key<M>
    public fun model(vararg options: Options.Read): M

    public fun duplicate(): Cursor<M>

}

public fun <M : Any> Cursor<M>.models(): Sequence<M> = sequence {
    use {
        while (isValid()) {
            yield(model())
            next()
        }
    }
}

public data class Entry<M : Any>(val key: Key<M>, val model: M)

public fun <M : Any> Cursor<M>.entries(): Sequence<Entry<M>> = sequence {
    use {
        while (isValid()) {
            yield(Entry(key(), model()))
            next()
        }
    }
}
