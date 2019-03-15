package org.kodein.db.leveldb.jvm

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.jni.LevelDBJNI


object LevelDBJVM : LevelDB.Factory by LevelDBJNI.Factory {
    init {
        System.loadLibrary("kodein-leveldb-jni-lib-jvm")
    }
}
