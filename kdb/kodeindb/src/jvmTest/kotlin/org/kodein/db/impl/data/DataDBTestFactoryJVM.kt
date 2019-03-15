package org.kodein.db.impl.data

import org.kodein.db.data.DataDB
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.jvm.LevelDBJVM

private val platformFactory = LevelDB.Factory.Based("/tmp/", LevelDBJVM)

actual object DataDBTestFactory {
    actual fun open(): DataDB = DataDBImpl(platformFactory.open("datadb"))
    actual fun destroy() = platformFactory.destroy("datadb")
}
