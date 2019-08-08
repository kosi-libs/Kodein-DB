package org.kodein.db

import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.use

interface DBCursorEntry<M : Any> {
    fun transientKey(): TransientKey<M>
    fun model(vararg options: Options.Read): M
    fun transientSeekKey(): TransientBytes
}

interface DBCursor<M: Any> : DBCursorEntry<M>, Closeable {

    fun isValid(): Boolean

    fun next()
    fun prev()

    fun seekToFirst()
    fun seekToLast()
    fun seekTo(target: ReadBuffer)

    fun nextEntries(size: Int): Entries<M>

    interface Entries<M: Any> : Closeable {
        val size: Int
        fun seekKey(i: Int): ReadBuffer
        fun key(i: Int): Key<M>
        operator fun get(i: Int, vararg options: Options.Read): M
    }

}

fun <M : Any> DBCursor<M>.models(): Sequence<M> = sequence {
    use {
        while (isValid())
            yield(model())
    }
}

fun <M : Any> DBCursor<M>.entries(): Sequence<DBCursorEntry<M>> = sequence {
    use {
        while (isValid())
            yield(this@entries)
    }
}

fun <M : Any> DBCursor.Entries<M>.models(): Iterable<M> = object : Iterable<M> {
    override fun iterator(): Iterator<M> = iterator {
        for (i in 0 until size)
            yield(get(i))
    }
}
