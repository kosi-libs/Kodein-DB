package org.kodein.db.impl.model

import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.data.DataCursor
import org.kodein.db.impl.data.getDocumentKeyID
import org.kodein.db.model.ModelCursor
import org.kodein.memory.Closeable
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.getBytes
import org.kodein.memory.io.wrap
import kotlin.reflect.KClass

internal class ModelCursorImpl<B : Any, M : B>(override val cursor: DataCursor, private val mdb: ModelDBImpl, private val modelType: KClass<M>) : ModelCursor<M>, ResettableCursorModule, Closeable by cursor {

    private var key: Key<M>? = null
    private var model: Sized<M>? = null

    override fun reset() {
        key = null
        model = null
    }

    override fun key() = key ?: Key<M>(KBuffer.wrap(cursor.transientKey().getBytes(0))).also { key = it }

    override fun model(vararg options: Options.Read): Sized<M> = model ?: mdb.deserialize(modelType, getDocumentKeyID(key().bytes), cursor.transientValue(), options).also { model = it }

    override fun duplicate(): ModelCursor<M> = ModelCursorImpl(cursor.duplicate(), mdb, modelType)
}
