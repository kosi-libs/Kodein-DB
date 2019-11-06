package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.Key
import org.kodein.db.DBListener
import org.kodein.db.data.DataBatch
import org.kodein.db.model.ModelBatch
import org.kodein.memory.Closeable
import org.kodein.memory.closeAll
import org.kodein.memory.useAll
import org.kodein.memory.util.MaybeThrowable
import org.kodein.memory.util.forEachCatchTo
import org.kodein.memory.util.forEachResilient

internal class ModelBatchImpl(override val mdb: ModelDBImpl, override val data: DataBatch) : ModelWriteModule, ModelBatch, Closeable by data {

    private val didActions = ArrayList<DBListener<Any>.() -> Unit>()

    override fun willAction(action: DBListener<Any>.() -> Unit) = mdb.getListeners().forEach(action)

    override fun didAction(action: DBListener<Any>.() -> Unit) { didActions.add(action) }

    override fun write(afterErrors: MaybeThrowable, vararg options: Options.Write) {
        data.write(afterErrors, *options)

        val listeners = mdb.getListeners()
        didActions.forEachCatchTo(afterErrors) { listeners.forEachResilient(it) }
    }
}
