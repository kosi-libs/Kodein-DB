package org.kodein.db.leveldb.android

import android.content.Context
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.jni.LevelDBJNI


object LevelDBAndroid : LevelDB.Factory by LevelDBJNI.Factory {

    init {
        System.loadLibrary("leveldb-jni-native")
    }

    fun internal(context: Context) : LevelDB.Factory = LevelDB.Factory.Based(context.filesDir.absolutePath + "/", LevelDBJNI.Factory)

    fun external(context: Context) : LevelDB.Factory = LevelDB.Factory.Based(context.getExternalFilesDir(null).absolutePath + "/", LevelDBJNI.Factory)

    fun internalCache(context: Context) : LevelDB.Factory = LevelDB.Factory.Based(context.cacheDir.absolutePath + "/", LevelDBJNI.Factory)

    fun externalCache(context: Context) : LevelDB.Factory = LevelDB.Factory.Based(context.externalCacheDir.absolutePath + "/", LevelDBJNI.Factory)
}
