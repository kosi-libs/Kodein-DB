package org.kodein.db.leveldb.jvm

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.jni.LevelDBNative


object LevelDBJVM : LevelDB.Factory by LevelDBNative.Factory {
    init {
        System.loadLibrary("leveldb-jni-native")
    }
}
