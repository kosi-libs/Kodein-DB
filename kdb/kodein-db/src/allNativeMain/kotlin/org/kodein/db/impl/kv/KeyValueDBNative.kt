package org.kodein.db.impl.kv

import org.kodein.db.DBFactory
import org.kodein.db.kv.KeyValueDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.native.LevelDBNative


public object KeyValueDBNative : AbstractKeyValueDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBNative
}

public actual val KeyValueDB.Companion.default: DBFactory<KeyValueDB> get() = KeyValueDBNative
