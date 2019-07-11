package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.data.DataDB
import org.kodein.db.model.MetadataExtractor
import org.kodein.db.model.ModelDB
import org.kodein.db.model.Serializer
import org.kodein.db.react.DBListener
import org.kodein.memory.util.forEachResilient

internal class ModelBatchImpl(override val mdb: ModelDBImpl, override val data: DataDB.Batch) : BaseModelWrite, ModelDB.Batch {

    private val didActions = ArrayList<DBListener.() -> Unit>()

    override fun didAction(action: DBListener.() -> Unit) { didActions.add(action) }

    override fun write(vararg options: Options.Write) {
        data.write(*options)

        didActions.forEachResilient { action ->
            mdb.listeners.toList().forEachResilient(action)
        }
    }

    override fun close() = data.close()
}
