package org.kodein.db.impl.data

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.jvm.LevelDBJvm


public object DataDBJvm : AbstractDataDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBJvm
}

public actual val DataDB.Companion.default: DBFactory<DataDB> get() = DataDBJvm
