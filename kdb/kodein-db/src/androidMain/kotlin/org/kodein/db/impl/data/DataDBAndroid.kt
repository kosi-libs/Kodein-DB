package org.kodein.db.impl.data

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.android.LevelDBAndroid


object DataDBAndroid : AbstractDataDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBAndroid
}

actual val DataDB.Companion.default: DBFactory<DataDB> get() = DataDBAndroid
