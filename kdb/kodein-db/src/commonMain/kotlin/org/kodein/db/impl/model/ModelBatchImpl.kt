package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.data.DataDB
import org.kodein.db.model.MetadataExtractor
import org.kodein.db.model.ModelDB
import org.kodein.db.model.Serializer

internal class ModelBatchImpl(override val mdb: ModelDBImpl, override val data: DataDB.Batch) : BaseModelWrite, ModelDB.Batch {

    override fun write(vararg options: Options.Write) = data.write(*options)

    override fun close() = data.close()
}
