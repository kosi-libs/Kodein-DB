package org.kodein.db.data.native

import org.kodein.db.impl.data.AbstractDataDBFactory
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.native.LevelDBNative


object DataDBNative : AbstractDataDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBNative
}
