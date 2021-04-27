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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class LDBTests_04_ReOpen : LevelDBTests() {

    class LDB : LDBTests_04_ReOpen() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_04_ReOpen() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_PutCloseOpenGet() {
        ldb!!.put(native("key"), native("value"))

        ldb!!.close()

        ldb = factory.open("db", options().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        val value = ldb!!.get(native("key"))!!
        assertBytesEquals(array("value"), value)
        value.close()
    }

    @Test
    fun test_01_PutCloseOpenIter() {
        ldb!!.put(native("key"), native("value"))

        ldb!!.close()

        ldb = factory.open("db", options().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToFirst()

            assertTrue(cursor.isValid())
            assertBytesEquals(array("key"), cursor.transientKey())
            assertBytesEquals(array("value"), cursor.transientValue())

            cursor.next()
            assertFalse(cursor.isValid())
        }
    }

}
