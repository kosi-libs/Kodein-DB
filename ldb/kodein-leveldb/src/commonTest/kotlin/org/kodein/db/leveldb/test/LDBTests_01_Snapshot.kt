package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.default
import org.kodein.db.leveldb.inDir
import org.kodein.db.leveldb.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class LDBTests_01_Snapshot : LevelDBTests() {

    class LDB : LDBTests_01_Snapshot() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_01_Snapshot() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_PutGet() {
        ldb!!.newSnapshot().use { snapshot ->
            ldb!!.put(buffer("key"), buffer("newValueBuffer"))

            assertNull(ldb!!.get(buffer("key"), LevelDB.ReadOptions(snapshot = snapshot)))
        }
    }

    @Test
    fun test_01_PutDeleteGet() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        ldb!!.newSnapshot().use { snapshot ->
            ldb!!.delete(buffer("key"))

            val value = ldb!!.get(buffer("key"), LevelDB.ReadOptions(snapshot = snapshot))!!
            assertBytesEquals(byteArray("newValueBuffer"), value)
            value.close()
        }
    }

}