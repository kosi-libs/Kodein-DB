package org.kodein.db.leveldb.android

import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.jni.LevelDBJNI


public object LevelDBAndroid : LevelDBFactory by LevelDBJNI.Factory {
    init {
        System.loadLibrary("kodein-leveldb-jni")
    }
}
