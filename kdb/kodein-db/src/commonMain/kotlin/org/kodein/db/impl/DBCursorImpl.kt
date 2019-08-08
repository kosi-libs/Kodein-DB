package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.model.ModelCursor
import org.kodein.memory.io.ReadBuffer

internal class DBCursorImpl<M : Any>(private val cursor: ModelCursor<M>) : DBCursor<M> {

    override fun isValid(): Boolean = cursor.isValid()

    override fun next() = cursor.next()

    override fun prev() = cursor.prev()

    override fun seekToFirst() = cursor.seekToFirst()

    override fun seekToLast() = cursor.seekToLast()

    override fun seekTo(target: ReadBuffer) = cursor.seekTo(target)

    override fun transientKey(): TransientKey<M> = cursor.transientKey()

    override fun transientSeekKey(): TransientBytes = cursor.transientSeekKey()

    override fun model(vararg options: Options.Read): M = cursor.model(*options).value

    override fun nextEntries(size: Int): DBCursor.Entries<M> = EntriesImpl(cursor.nextEntries(size))

    internal class EntriesImpl<M : Any>(private val entries : ModelCursor.Entries<M>) : DBCursor.Entries<M> {

        override val size: Int get() = entries.size

        override fun key(i: Int): Key<M> = entries.key(i)

        override fun seekKey(i: Int): ReadBuffer = entries.seekKey(i)

        @Suppress("ReplaceGetOrSet")
        override fun get(i: Int, vararg options: Options.Read): M = entries.get(i, *options).value

        override fun close() = entries.close()
    }

    override fun close() = cursor.close()
}
