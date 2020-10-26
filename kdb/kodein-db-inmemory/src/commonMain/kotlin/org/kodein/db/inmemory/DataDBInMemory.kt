package org.kodein.db.inmemory

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.impl.data.AbstractDataDBFactory
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.inmemory.LevelDBInMemory


public object DataDBInMemory : AbstractDataDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBInMemory
}

public val DataDB.Companion.inMemory: DBFactory<DataDB> get() = DataDBInMemory
