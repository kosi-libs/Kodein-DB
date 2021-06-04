package org.kodein.db.impl.model

import org.kodein.db.DBListener
import org.kodein.db.Key
import org.kodein.db.KeyAndSize
import org.kodein.db.Options
import org.kodein.db.data.DataBatch
import org.kodein.db.model.ModelBatch
import org.kodein.db.model.ModelDBListener
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable
import org.kodein.memory.util.forEachCatchTo
import org.kodein.memory.util.forEachResilient
import kotlin.reflect.KClass

internal class ModelBatchImpl(override val mdb: ModelDBImpl, override val data: DataBatch) : ModelWriteModule<DataBatch>, ModelBatch, Closeable by data {

    private val didActions = ArrayList<ModelDBListener<Any>.() -> Unit>()

    override fun willAction(action: ModelDBListener<Any>.() -> Unit) = mdb.getListeners().forEach(action)

    override fun didAction(action: ModelDBListener<Any>.() -> Unit) { didActions.add(action) }

    override fun <M : Any> put(model: M, vararg options: Options.BatchPut): KeyAndSize<M> =
        put(model, options, DataBatch::put)

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.BatchPut): Int =
        put(key, model, options, DataBatch::put)

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.BatchDelete) {
        delete(type, key, options, DataBatch::delete)
    }

    override fun write(afterErrors: MaybeThrowable, vararg options: Options.BatchWrite) {
        data.write(afterErrors, *options)

        val listeners = mdb.getListeners()
        didActions.forEachCatchTo(afterErrors) { listeners.forEachResilient(it) }
    }
}
