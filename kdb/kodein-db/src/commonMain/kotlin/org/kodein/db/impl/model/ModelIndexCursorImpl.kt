package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.data.DataIndexCursor
import org.kodein.db.impl.data.getDocumentKeyID
import org.kodein.db.model.ModelIndexCursor
import org.kodein.memory.io.ReadMemory
import kotlin.reflect.KClass


internal class ModelIndexCursorImpl<M : Any>(override val cursor: DataIndexCursor, mdb: ModelDBImpl, modelType: KClass<M>) : ModelCursorImpl<M>(cursor, mdb, modelType), ModelIndexCursor<M> {

    private class AssociatedObject(val value: Any?)

    private var associatedObject: AssociatedObject? = null

    override fun reset() {
        super.reset()
        associatedObject = null
    }

    override fun transientAssociatedData(): ReadMemory? = cursor.transientAssociatedData()

}
