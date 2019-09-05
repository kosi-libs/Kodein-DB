package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.model.ModelCursor
import org.kodein.memory.io.ReadBuffer

internal class DBCursorImpl<M : Any>(private val cursor: ModelCursor<M>) : DBCursor<M>, BaseCursor by cursor {

    override fun transientKey(): TransientKey<M> = cursor.transientKey()

    override fun model(vararg options: Options.Read): M = cursor.model(*options).value

    override fun nextEntries(size: Int): DBCursor.Entries<M> = EntriesImpl(cursor.nextEntries(size))

    internal class EntriesImpl<M : Any>(private val entries : ModelCursor.Entries<M>) : DBCursor.Entries<M>, BaseCursor.BaseEntries by entries {

        override fun key(i: Int): Key<M> = entries.key(i)

        @Suppress("ReplaceGetOrSet")
        override fun get(i: Int, vararg options: Options.Read): M = entries.get(i, *options).value
    }

}
