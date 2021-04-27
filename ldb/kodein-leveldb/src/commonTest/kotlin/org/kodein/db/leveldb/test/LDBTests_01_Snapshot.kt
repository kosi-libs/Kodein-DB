package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.default
import org.kodein.db.leveldb.inDir
import org.kodein.db.leveldb.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.array
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
            ldb!!.put(native("key"), native("newValueBuffer"))

            assertNull(ldb!!.get(native("key"), LevelDB.ReadOptions(snapshot = snapshot)))
        }
    }

    @Test
    fun test_01_PutDeleteGet() {
        ldb!!.put(native("key"), native("newValueBuffer"))

        ldb!!.newSnapshot().use { snapshot ->
            ldb!!.delete(native("key"))

            val value = ldb!!.get(native("key"), LevelDB.ReadOptions(snapshot = snapshot))!!
            assertBytesEquals(array("newValueBuffer"), value)
            value.close()
        }
    }

}