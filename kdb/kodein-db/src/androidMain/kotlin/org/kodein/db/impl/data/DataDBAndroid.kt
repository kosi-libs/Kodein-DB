package org.kodein.db.impl.data

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.impl.kv.KeyValueDBAndroid
import org.kodein.db.kv.KeyValueDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.android.LevelDBAndroid


public object DataDBAndroid : AbstractDataDBFactory() {
    override val kvdbFactory: DBFactory<KeyValueDB> = KeyValueDBAndroid
}

public actual val DataDB.Companion.default: DBFactory<DataDB> get() = DataDBAndroid
