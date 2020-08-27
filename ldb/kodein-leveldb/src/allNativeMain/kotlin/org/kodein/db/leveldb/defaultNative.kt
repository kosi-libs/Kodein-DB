package org.kodein.db.leveldb

import org.kodein.db.leveldb.native.LevelDBNative

public actual val LevelDB.Companion.default: LevelDBFactory get() = LevelDBNative
