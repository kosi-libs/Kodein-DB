package org.kodein.db.impl.data

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.native.LevelDBNative


object DataDBNative : AbstractDataDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBNative
}

actual val DataDB.Companion.default: DBFactory<DataDB> get() = DataDBNative
