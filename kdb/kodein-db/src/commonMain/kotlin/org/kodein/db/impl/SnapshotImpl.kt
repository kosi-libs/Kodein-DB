package org.kodein.db.impl

import org.kodein.db.KeyMaker
import org.kodein.db.Snapshot
import org.kodein.db.model.ModelSnapshot
import org.kodein.memory.Closeable

internal class SnapshotImpl(override val mdb: ModelSnapshot) : Snapshot, DBReadModule, KeyMaker by mdb, Closeable by mdb
