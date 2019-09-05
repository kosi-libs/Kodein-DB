package org.kodein.db.leveldb

import org.kodein.db.leveldb.jvm.LevelDBJvm

actual val LevelDB.Companion.default: LevelDBFactory get() = LevelDBJvm
