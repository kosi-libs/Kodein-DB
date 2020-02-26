package org.kodein.db.impl

import org.kodein.db.BaseCursor
import org.kodein.db.Cursor
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.model.ModelCursor

internal class CursorImpl<M : Any>(private val cursor: ModelCursor<M>) : Cursor<M>, BaseCursor by cursor {

    override fun key(): Key<M> = cursor.key()

    override fun model(vararg options: Options.Read): M = cursor.model(*options).model

    override fun duplicate(): Cursor<M> = CursorImpl(cursor.duplicate())

}
