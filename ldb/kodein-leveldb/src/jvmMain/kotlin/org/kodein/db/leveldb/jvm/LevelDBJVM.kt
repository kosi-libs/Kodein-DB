package org.kodein.db.leveldb.jvm

import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.jni.LevelDBJNI


object LevelDBJVM : LevelDBFactory by LevelDBJNI.Factory {
    init {
        System.loadLibrary("kodein-leveldb")
    }
}
