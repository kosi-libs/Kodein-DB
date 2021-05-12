package org.kodein.db.inmemory

import org.kodein.db.DBFactory
import org.kodein.db.impl.kv.AbstractKeyValueDBFactory
import org.kodein.db.kv.KeyValueDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.inmemory.LevelDBInMemory


public object KeyValueDBInMemory : AbstractKeyValueDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBInMemory
}

public val KeyValueDB.Companion.inMemory: DBFactory<KeyValueDB> get() = KeyValueDBInMemory
