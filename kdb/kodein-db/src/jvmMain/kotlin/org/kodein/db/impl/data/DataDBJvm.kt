package org.kodein.db.impl.data

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.impl.kv.KeyValueDBJvm
import org.kodein.db.kv.KeyValueDB


public object DataDBJvm : AbstractDataDBFactory() {
    override val kvdbFactory: DBFactory<KeyValueDB> = KeyValueDBJvm
}

public actual val DataDB.Companion.default: DBFactory<DataDB> get() = DataDBJvm
