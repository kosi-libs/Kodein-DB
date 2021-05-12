package org.kodein.db.impl.data

import org.kodein.db.DBFactory
import org.kodein.db.data.DataDB
import org.kodein.db.impl.kv.KeyValueDBNative
import org.kodein.db.kv.KeyValueDB


public object DataDBNative : AbstractDataDBFactory() {
    override val kvdbFactory: DBFactory<KeyValueDB> = KeyValueDBNative
}

public actual val DataDB.Companion.default: DBFactory<DataDB> get() = DataDBNative
