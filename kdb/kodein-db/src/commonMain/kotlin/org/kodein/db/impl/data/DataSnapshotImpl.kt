package org.kodein.db.impl.data

import org.kodein.db.data.DataSnapshot
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable

internal class DataSnapshotImpl(override val ldb: LevelDB, override val snapshot: LevelDB.Snapshot) : DataReadModule, DataSnapshot, Closeable by snapshot
