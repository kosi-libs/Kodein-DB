package org.kodein.db.impl

import org.kodein.db.DBSnapshot
import org.kodein.db.KeyMaker
import org.kodein.db.model.ModelSnapshot
import org.kodein.memory.Closeable

internal class DBSnapshotImpl(override val mdb: ModelSnapshot) : DBSnapshot, DBReadBase, KeyMaker by mdb, Closeable by mdb
