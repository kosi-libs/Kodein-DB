package org.kodein.db

import org.kodein.memory.use

public interface Cursor<M: Any> : BaseCursor {

    public fun key(): Key<M>
    public fun model(vararg options: Options.Read): M

    public fun duplicate(): Cursor<M>

}

public data class Entry<M : Any>(val key: Key<M>, val model: M)

@PublishedApi
internal inline fun <M : Any, T, R> Cursor<M>.use(reverse: Boolean, seekToStart: Boolean, block: (Sequence<T>) -> R, crossinline get: Cursor<M>.() -> T): R {
    use {
        if (seekToStart) {
            if (reverse) seekToLast()
            else seekToFirst()
        }

        return block(sequence {
            while (isValid()) {
                yield(get())
                if (reverse) prev()
                else next()
            }
        })
    }
}

public inline fun <M : Any, R> Cursor<M>.useModels(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Read, block: (Sequence<M>) -> R): R =
        use(reverse, seekToStart, block) { model(*options) }

public fun <M : Any, R> Cursor<M>.useKeys(reverse: Boolean = false, seekToStart: Boolean = true, block: (Sequence<Key<M>>) -> R): R =
        use(reverse, seekToStart, block) { key() }

public fun <M : Any, R> Cursor<M>.useEntries(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Read, block: (Sequence<Entry<M>>) -> R): R =
        use(reverse, seekToStart, block) { Entry(key(), model(*options)) }
