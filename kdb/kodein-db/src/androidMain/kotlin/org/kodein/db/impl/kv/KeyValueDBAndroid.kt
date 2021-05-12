package org.kodein.db.impl.kv

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.kv.KeyValueDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.android.LevelDBAndroid


public object KeyValueDBAndroid : AbstractKeyValueDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBAndroid
}

public actual val KeyValueDB.Companion.default: DBFactory<KeyValueDB> get() = KeyValueDBAndroid
