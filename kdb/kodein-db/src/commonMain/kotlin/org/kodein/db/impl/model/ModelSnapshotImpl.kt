package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.data.DataDB
import org.kodein.db.model.ModelDB
import org.kodein.db.model.Serializer

internal class ModelSnapshotImpl(override val mdb: ModelDBImpl, override val data: DataDB.Snapshot) : BaseModelRead, ModelDB.Snapshot {

    override fun close() = data.close()

}
