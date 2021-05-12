package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.data.DataIndexCursor
import org.kodein.db.impl.data.getDocumentKeyID
import org.kodein.db.model.ModelIndexCursor
import kotlin.reflect.KClass


internal class ModelIndexCursorImpl<M : Any>(override val cursor: DataIndexCursor, mdb: ModelDBImpl, modelType: KClass<M>) : ModelCursorImpl<M>(cursor, mdb, modelType), ModelIndexCursor<M> {

    private class AssociatedObject(val value: Any?)

    private var associatedObject: AssociatedObject? = null

    override fun reset() {
        super.reset()
        associatedObject = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> associatedObject(type: KClass<T>, vararg options: Options.Get): T? {
        associatedObject?.let { return it.value as T? }

        val associatedData = cursor.transientAssociatedData()

        val associatedObject =
            if (associatedData != null) AssociatedObject(mdb.deserialize(type, getDocumentKeyID(key().bytes), associatedData, options).model)
            else AssociatedObject(null)
        this.associatedObject = associatedObject
        return associatedObject.value as T?
    }

}
