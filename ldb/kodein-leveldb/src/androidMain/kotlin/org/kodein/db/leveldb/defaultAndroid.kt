package org.kodein.db.leveldb

import org.kodein.db.leveldb.android.LevelDBAndroid

public actual val LevelDB.Companion.default: LevelDBFactory get() = LevelDBAndroid
