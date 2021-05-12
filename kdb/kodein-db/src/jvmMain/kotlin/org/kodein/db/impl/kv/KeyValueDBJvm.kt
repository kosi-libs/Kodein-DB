package org.kodein.db.impl.kv

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.kv.KeyValueDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.jvm.LevelDBJvm


public object KeyValueDBJvm : AbstractKeyValueDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBJvm
}

public actual val KeyValueDB.Companion.default: DBFactory<KeyValueDB> get() = KeyValueDBJvm
