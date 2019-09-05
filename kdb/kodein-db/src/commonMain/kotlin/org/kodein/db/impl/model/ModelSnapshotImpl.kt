package org.kodein.db.impl.model

import org.kodein.db.data.DataSnapshot
import org.kodein.db.model.ModelSnapshot
import org.kodein.memory.Closeable

internal class ModelSnapshotImpl(override val mdb: ModelDBImpl, override val data: DataSnapshot) : ModelReadBaseImpl, ModelSnapshot, Closeable by data
