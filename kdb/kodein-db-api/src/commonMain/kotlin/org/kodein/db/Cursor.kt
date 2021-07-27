package org.kodein.db

import org.kodein.memory.use

public interface Cursor<M: Any> : BaseCursor {

    public fun key(): Key<M>
    public fun model(vararg options: Options.Get): M

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

public fun <M : Any> Cursor<M>.asModelSequence(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Get): Sequence<M> =
        asSequence(reverse, seekToStart) { model(*options) }

public inline fun <M : Any, R> Cursor<M>.useModels(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Get, block: (Sequence<M>) -> R): R =
        use { block(asModelSequence(reverse, seekToStart, *options)) }

public fun <M : Any> Cursor<M>.asKeySequence(reverse: Boolean = false, seekToStart: Boolean = true): Sequence<Key<M>> =
        asSequence(reverse, seekToStart) { key() }

public fun <M : Any, R> Cursor<M>.useKeys(reverse: Boolean = false, seekToStart: Boolean = true, block: (Sequence<Key<M>>) -> R): R =
        use { block(asKeySequence(reverse, seekToStart)) }

public fun <M : Any> Cursor<M>.asEntrySequence(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Get): Sequence<Entry<M>> =
        asSequence(reverse, seekToStart) { Entry(key(), model(*options)) }

public fun <M : Any, R> Cursor<M>.useEntries(reverse: Boolean = false, seekToStart: Boolean = true, vararg options: Options.Get, block: (Sequence<Entry<M>>) -> R): R =
        use { block(asEntrySequence(reverse, seekToStart, *options)) }


/**
 * Consumes the cursor and returns whether it contains any entry.
 *
 * @return true, if there's one entry in the cursor
 */
public fun <M : Any> Cursor<M>.any(): Boolean = use { it.isValid() }

/**
 * Consumes the cursor and returns whether it is empty.
 *
 * @return true, if there's no entry in the cursor
 */
public fun <M : Any> Cursor<M>.none(): Boolean = use { !it.isValid() }

/**
 * Consumes the cursor and returns a [List] containing
 * all elements in the database from this cursor.
 */
public fun <M : Any> Cursor<M>.toModelList(): List<M> = useModels { it.toList() }

/**
 * Consumes the cursor and returns a [List] containing
 * all elements in the database from this cursor.
 */
public fun <M : Any> Cursor<M>.toKeyList(): List<Key<M>> = useKeys { it.toList() }

/**
 * Consumes the cursor and returns a [List] containing
 * all elements in the database from this cursor.
 */
public fun <M : Any> Cursor<M>.toEntryList(): List<Entry<M>> = useEntries { it.toList() }

/**
 * Consumes the cursor and returns the current element of this cursor.
 *
 * @throws NoSuchElementException if the cursor is empty.
 */
public fun <M : Any> Cursor<M>.first(): M = use {
    if (!it.isValid()) throw NoSuchElementException()
    it.model()
}

/**
 * Consumes the cursor and returns the current element of this cursor
 * or null if the cursor is empty.
 */
public fun <M : Any> Cursor<M>.firstOrNull(): M? = use {
    if (!it.isValid()) null
    else it.model()
}
