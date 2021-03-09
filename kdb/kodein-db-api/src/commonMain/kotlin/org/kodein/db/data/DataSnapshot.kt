package org.kodein.db.data

import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable


public interface DataSnapshot : DataRead, Closeable {
    public val snapshot: LevelDB.Snapshot
}
