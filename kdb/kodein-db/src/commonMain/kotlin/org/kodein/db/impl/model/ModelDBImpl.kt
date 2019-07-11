package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.data.DataDB
import org.kodein.db.model.*
import org.kodein.db.react.DBListener
import org.kodein.memory.Closeable
import org.kodein.memory.util.forEachResilient

internal class ModelDBImpl(val serializer: Serializer, private val metadataExtractor: MetadataExtractor, val typeTable: TypeTable, override val data: DataDB) : ModelDB, BaseModelRead, BaseModelWrite {

    internal val listeners = LinkedHashSet<DBListener>()

    override fun didAction(action: DBListener.() -> Unit) = listeners.toList().forEachResilient(action)

    internal fun getMetadata(model: Any, options: Array<out Options.Write>) =
            (model as? HasMetadata)?.getMetadata(this, *options) ?: metadataExtractor.extractMetadata(model, *options)

    override val mdb: ModelDBImpl get() = this

    override fun newBatch(): ModelDB.Batch = ModelBatchImpl(this, data.newBatch())

    override fun newSnapshot(vararg options: Options.Read): ModelDB.Snapshot = ModelSnapshotImpl(this, data.newSnapshot())

    override fun close() = data.close()

    override fun register(listener: DBListener, vararg options: Options.React): Closeable {
        val subscription = Closeable { listeners -= listener }
        if (listeners.add(listener))  listener.setSubscription(subscription)
        return subscription
    }

}
