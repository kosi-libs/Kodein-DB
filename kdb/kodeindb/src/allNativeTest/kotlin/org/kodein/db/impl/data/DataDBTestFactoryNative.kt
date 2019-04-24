package org.kodein.db.impl.data

import org.kodein.db.data.DataDB
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.native.LevelDBNative

private val platformFactory = LevelDB.Factory.Based("/tmp/", LevelDBNative)

actual object DataDBTestFactory {
    actual fun open(): DataDB = DataDBImpl(platformFactory.open("datadb"))
    actual fun destroy() = platformFactory.destroy("datadb")
}
