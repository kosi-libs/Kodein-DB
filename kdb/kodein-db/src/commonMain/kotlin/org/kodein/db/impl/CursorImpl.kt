package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.model.ModelCursor

internal class CursorImpl<M : Any>(private val cursor: ModelCursor<M>) : Cursor<M>, BaseCursor by cursor {

    override fun transientKey(): Key.Transient<M> = cursor.transientKey()

    override fun model(vararg options: Options.Read): M = cursor.model(*options).value

    override fun nextEntries(size: Int): Cursor.Entries<M> = EntriesImpl(cursor.nextEntries(size))

    internal class EntriesImpl<M : Any>(private val entries : ModelCursor.Entries<M>) : Cursor.Entries<M>, BaseCursor.BaseEntries by entries {

        override fun key(i: Int): Key<M> = entries.key(i)

        @Suppress("ReplaceGetOrSet")
        override fun get(i: Int, vararg options: Options.Read): M = entries.get(i, *options).value
    }

}
