package org.kodein.db

import org.kodein.memory.use

public interface Cursor<M: Any> : BaseCursor {

    public fun key(): Key<M>
    public fun model(vararg options: Options.Read): M

    public fun duplicate(): Cursor<M>

}

public data class Entry<M : Any>(val key: Key<M>, val model: M)

private inline fun <M : Any, T> Cursor<M>.asSequence(reverse: Boolean, seekToStart: Boolean, crossinline get: Cursor<M>.() -> T): Sequence<T> {
    if (seekToStart) {
        if (reverse) seekToLast()
        else seekToFirst()
    }

    return sequence {
        while (isValid()) {
            yield(get())
            if (reverse) prev()
            else next()
        }
    }
}

public fun <M : Any> Cursor<M>.asModelSequence(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Read): Sequence<M> =
        asSequence(reverse, seekToStart) { model(*options) }

public inline fun <M : Any, R> Cursor<M>.useModels(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Read, block: (Sequence<M>) -> R): R =
        use { block(asModelSequence(reverse, seekToStart, *options)) }

public fun <M : Any> Cursor<M>.asKeySequence(reverse: Boolean = false, seekToStart: Boolean = true): Sequence<Key<M>> =
        asSequence(reverse, seekToStart) { key() }

public fun <M : Any, R> Cursor<M>.useKeys(reverse: Boolean = false, seekToStart: Boolean = true, block: (Sequence<Key<M>>) -> R): R =
        use { block(asKeySequence(reverse, seekToStart)) }

public fun <M : Any> Cursor<M>.asEntrySequence(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Read): Sequence<Entry<M>> =
        asSequence(reverse, seekToStart) { Entry(key(), model(*options)) }

public fun <M : Any, R> Cursor<M>.useEntries(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Read, block: (Sequence<Entry<M>>) -> R): R =
        use { block(asEntrySequence(reverse, seekToStart, *options)) }
