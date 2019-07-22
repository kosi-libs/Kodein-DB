package org.kodein.db.data.jvm

import org.kodein.db.impl.data.AbstractDataDBFactory
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.jvm.LevelDBJVM


object DataDBJVM : AbstractDataDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBJVM
}
