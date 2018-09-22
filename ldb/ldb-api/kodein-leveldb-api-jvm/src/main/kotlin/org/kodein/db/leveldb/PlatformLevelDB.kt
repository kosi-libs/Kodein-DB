package org.kodein.db.leveldb

import java.nio.ByteBuffer

actual interface PlatformLevelDB {
    fun put(key: ByteBuffer, value: ByteBuffer, options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT)
    fun delete(key: ByteBuffer, options: LevelDB.WriteOptions = LevelDB.WriteOptions.DEFAULT)
    fun get(key: ByteBuffer, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): Allocation?
    fun indirectGet(key: ByteBuffer, options: LevelDB.ReadOptions = LevelDB.ReadOptions.DEFAULT): Allocation?

    actual interface WriteBatch {
        fun put(key: ByteBuffer, value: ByteBuffer)
        fun delete(key: ByteBuffer)
    }

    actual interface Cursor {
        fun seekTo(target: ByteBuffer)
    }
}
