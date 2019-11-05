package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataCursor
import org.kodein.db.impl.data.getObjectKeyID
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.orm.Serializer
import kotlin.reflect.KClass

internal class ModelCursorImpl<B : Any, M : B>(private val dc: DataCursor, private val mdb: ModelDBImpl, private val modelType: KClass<M>) : ModelCursor<M>, BaseCursor by dc {

    override fun transientKey() = Key.Transient<M>(dc.transientKey())

    override fun model(vararg options: Options.Read): Sized<M> = ModelReadModule.getFrom(dc.transientValue(), getObjectKeyID(dc.transientKey()), modelType, mdb, options)

    private inner class Entries(private val dce: DataCursor.Entries) : ModelCursor.Entries<M>, BaseCursor.BaseEntries by dce {

        override fun key(i: Int) = Key.Heap<M>(dce.key(i))

        override fun get(i: Int, vararg options: Options.Read): Sized<M> = ModelReadModule.getFrom(dce[i], getObjectKeyID(dce.key(i)), modelType, mdb, options)

    }

    override fun nextEntries(size: Int): ModelCursor.Entries<M> = Entries(dc.nextEntries(size))

}
