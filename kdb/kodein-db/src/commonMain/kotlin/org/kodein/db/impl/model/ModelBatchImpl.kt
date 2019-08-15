package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.data.DataDB
import org.kodein.db.Key
import org.kodein.db.model.ModelDB
import org.kodein.db.DBListener
import org.kodein.memory.util.forEachResilient

internal class ModelBatchImpl(override val mdb: ModelDBImpl, override val data: DataDB.Batch) : BaseModelWrite, ModelDB.Batch {

    private val didActions = ArrayList<DBListener<Any>.() -> Unit>()

    override fun Key<*>.transform(): Key<*> = asHeapKey()

    override fun didAction(action: DBListener<Any>.() -> Unit) { didActions.add(action) }

    override fun write(vararg options: Options.Write) {
        data.write(*options)

        didActions.forEachResilient { action ->
            mdb.listeners.toList().forEachResilient(action)
        }
    }

    override fun close() = data.close()
}
