package org.kodein.db.impl.kv

import org.kodein.db.Options
import org.kodein.db.invoke
import org.kodein.db.kv.FillRawCache
import org.kodein.db.kv.FsSync
import org.kodein.db.kv.LevelDBOptions
import org.kodein.db.kv.VerifyChecksum
import org.kodein.db.leveldb.LevelDB


internal fun LevelDB.WriteOptions.Companion.from(options: Array<out Options>): LevelDB.WriteOptions {
    val syncOption: FsSync = options() ?: return DEFAULT
    return LevelDB.WriteOptions(sync = syncOption.sync)
}

internal fun LevelDB.ReadOptions.Companion.from(options: Array<out Options>, snapshot: LevelDB.Snapshot? = null): LevelDB.ReadOptions =
    options.fold(
        snapshot?.let { LevelDB.ReadOptions(snapshot = it) } ?: DEFAULT
    ) { l, o ->
        when (o) {
            is VerifyChecksum -> l.copy(verifyChecksums = o.verifyChecksums)
            is FillRawCache -> l.copy(fillCache = o.fillCache)
            else -> l
        }
    }
