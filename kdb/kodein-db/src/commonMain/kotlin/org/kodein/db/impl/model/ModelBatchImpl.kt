package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.Key
import org.kodein.db.DBListener
import org.kodein.db.data.DataBatch
import org.kodein.db.model.ModelBatch
import org.kodein.db.model.ModelDB
import org.kodein.memory.Closeable
import org.kodein.memory.util.forEachResilient

internal class ModelBatchImpl(override val mdb: ModelDBImpl, override val data: DataBatch) : ModelWriteBaseImpl, ModelBatch, Closeable by data {

    private val didActions = ArrayList<DBListener<Any>.() -> Unit>()

    override fun Key<*>.transform(): Key<*> = asHeapKey()

    override fun didAction(action: DBListener<Any>.() -> Unit) { didActions.add(action) }

    override fun write(vararg options: Options.Write) {
        data.write(*options)

        didActions.forEachResilient { action ->
            mdb.listeners.toList().forEachResilient(action)
        }
    }
}
