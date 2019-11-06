package org.kodein.db.impl.model

import org.kodein.db.BaseCursor
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.data.DataCursor
import org.kodein.db.impl.data.getObjectKeyID
import org.kodein.db.model.ModelCursor
import org.kodein.memory.Closeable
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.getBytesHere
import org.kodein.memory.io.wrap
import kotlin.reflect.KClass

internal class ModelCursorImpl<B : Any, M : B>(override val cursor: DataCursor, private val mdb: ModelDBImpl, private val modelType: KClass<M>) : ModelCursor<M>, ResettableCursorModule, Closeable by cursor {

    private var key: Key<M>? = null
    private var model: Sized<M>? = null

    override fun reset() {
        key = null
        model = null
    }

    override fun key() = key ?: Key<M>(KBuffer.wrap(cursor.transientKey().getBytesHere())).also { key = it }

    override fun model(vararg options: Options.Read): Sized<M> = model ?: ModelReadModule.getFrom(cursor.transientValue(), getObjectKeyID(cursor.transientKey()), modelType, mdb, options).also { model = it }

    private inner class Entries(private val dce: DataCursor.Entries) : ModelCursor.Entries<M>, BaseCursor.BaseEntries by dce {

        override fun key(i: Int) = Key<M>(dce.key(i))

        override fun get(i: Int, vararg options: Options.Read): Sized<M> = ModelReadModule.getFrom(dce[i], getObjectKeyID(dce.key(i)), modelType, mdb, options)

    }

    override fun nextEntries(size: Int): ModelCursor.Entries<M> = Entries(cursor.nextEntries(size))
}
