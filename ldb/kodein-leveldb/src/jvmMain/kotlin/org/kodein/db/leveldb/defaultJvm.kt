package org.kodein.db.leveldb

import org.kodein.db.leveldb.jvm.LevelDBJvm

public actual val LevelDB.Companion.default: LevelDBFactory get() = LevelDBJvm
