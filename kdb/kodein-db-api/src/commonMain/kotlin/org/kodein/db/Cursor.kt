package org.kodein.db

import org.kodein.memory.use

public interface Cursor<M: Any> : BaseCursor {

    public fun key(): Key<M>
    public fun model(vararg options: Options.Read): M

    public fun duplicate(): Cursor<M>

}

public fun <M : Any> Cursor<M>.useModels(): Sequence<M> = sequence {
    use {
        while (isValid()) {
            yield(model())
            next()
        }
    }
}

public fun <M : Any> Cursor<M>.useKeys(): Sequence<Key<M>> = sequence {
    use {
        while (isValid()) {
            yield(key())
            next()
        }
    }
}

public data class Entry<M : Any>(val key: Key<M>, val model: M)

public fun <M : Any> Cursor<M>.useEntries(): Sequence<Entry<M>> = sequence {
    use {
        while (isValid()) {
            yield(Entry(key(), model()))
            next()
        }
    }
}
