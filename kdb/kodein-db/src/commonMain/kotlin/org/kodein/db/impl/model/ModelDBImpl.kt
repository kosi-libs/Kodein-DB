package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.data.DataDB
import org.kodein.db.model.*

internal class ModelDBImpl(val serializer: Serializer, private val metadataExtractor: MetadataExtractor, val typeTable: TypeTable, override val data: DataDB) : ModelDB, BaseModelRead, BaseModelWrite {

    internal fun getMetadata(model: Any, options: Array<out Options.Write>) =
            (model as? HasMetadata)?.getMetadata(this, *options) ?: metadataExtractor.extractMetadata(model, *options)

    override val mdb: ModelDBImpl get() = this

    override fun newBatch(): ModelDB.Batch = ModelBatchImpl(this, data.newBatch())

    override fun newSnapshot(vararg options: Options.Read): ModelDB.Snapshot = ModelSnapshotImpl(this, data.newSnapshot())

    override fun close() = data.close()

}
