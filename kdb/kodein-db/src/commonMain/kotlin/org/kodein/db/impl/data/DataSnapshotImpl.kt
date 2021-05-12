package org.kodein.db.impl.data

import org.kodein.db.data.DataSnapshot
import org.kodein.db.kv.KeyValueSnapshot
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable

internal class DataSnapshotImpl(override val kv: KeyValueSnapshot) : DataReadModule, DataSnapshot, Closeable by kv {
    override fun currentOrNewSnapshot(): Pair<KeyValueSnapshot, Boolean> = kv to false
}
