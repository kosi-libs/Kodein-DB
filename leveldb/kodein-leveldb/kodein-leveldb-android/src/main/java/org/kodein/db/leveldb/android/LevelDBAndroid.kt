package org.kodein.db.leveldb.android

import android.content.Context
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.jni.LevelDBNative


object LevelDBAndroid : LevelDB.Factory by LevelDBNative.Factory {

    init {
        System.loadLibrary("leveldb-jni-native")
    }

    fun internal(context: Context) : LevelDB.Factory = LevelDB.Factory.Based(context.filesDir.absolutePath + "/", LevelDBNative.Factory)

    fun external(context: Context) : LevelDB.Factory = LevelDB.Factory.Based(context.getExternalFilesDir(null).absolutePath + "/", LevelDBNative.Factory)

    fun internalCache(context: Context) : LevelDB.Factory = LevelDB.Factory.Based(context.cacheDir.absolutePath + "/", LevelDBNative.Factory)

    fun externalCache(context: Context) : LevelDB.Factory = LevelDB.Factory.Based(context.externalCacheDir.absolutePath + "/", LevelDBNative.Factory)
}
