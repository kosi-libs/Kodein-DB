package org.kodein.db.data.android

import org.kodein.db.impl.data.AbstractDataDBFactory
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.android.LevelDBAndroid


object DataDBAndroid : AbstractDataDBFactory() {
    override val ldbFactory: LevelDBFactory get() = LevelDBAndroid
}
