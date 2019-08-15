package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.model.ModelDB

internal class SnapshotImpl(override val mdb: ModelDB.Snapshot) : DB.Snapshot, BaseDBRead {

    override fun close() = mdb.close()

}
