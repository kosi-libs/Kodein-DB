package org.kodein.db.impl.data

import org.kodein.db.data.DataDB
import org.kodein.db.leveldb.LevelDB

internal class DataSnapshotImpl(override val ldb: LevelDB, override val snapshot: LevelDB.Snapshot) : BaseDataRead, DataDB.Snapshot {

    override fun close() = snapshot.close()

}
