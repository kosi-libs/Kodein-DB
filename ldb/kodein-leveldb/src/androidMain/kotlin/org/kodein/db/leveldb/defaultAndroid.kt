package org.kodein.db.leveldb

import org.kodein.db.leveldb.android.LevelDBAndroid

actual val LevelDB.Companion.default: LevelDBFactory get() = LevelDBAndroid
