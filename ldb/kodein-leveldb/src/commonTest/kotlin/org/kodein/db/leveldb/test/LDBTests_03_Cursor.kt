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
import org.kodein.memory.util.deferScope
import kotlin.test.*

@Suppress("ClassName")
abstract class LDBTests_03_Cursor : LevelDBTests() {

    class LDB : LDBTests_03_Cursor() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_03_Cursor() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_Forward() {
        ldb!!.put(native(1), native("one"))
        ldb!!.put(native(2), native("two"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToFirst()

            assertTrue(cursor.isValid())
            assertBytesEquals(array(1), cursor.transientKey())
            assertBytesEquals(array("one"), cursor.transientValue())

            cursor.next()

            assertTrue(cursor.isValid())
            assertBytesEquals(array(2), cursor.transientKey())
            assertBytesEquals(array("two"), cursor.transientValue())

            cursor.next()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_01_Backward() {
        ldb!!.put(native(1), native("one"))
        ldb!!.put(native(2), native("two"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToLast()

            assertTrue(cursor.isValid())
            assertBytesEquals(array(2), cursor.transientKey())
            assertBytesEquals(array("two"), cursor.transientValue())

            cursor.prev()

            assertTrue(cursor.isValid())
            assertBytesEquals(array(1), cursor.transientKey())
            assertBytesEquals(array("one"), cursor.transientValue())

            cursor.prev()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_02_Seek() {
        ldb!!.put(native(1), native("one"))
        ldb!!.put(native(3), native("three"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekTo(native(2))

            assertTrue(cursor.isValid())
            assertBytesEquals(array(3), cursor.transientKey())
            assertBytesEquals(array("three"), cursor.transientValue())

            cursor.next()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_03_DirectSeek() {
        ldb!!.put(native(1), native("one"))
        ldb!!.put(native(3), native("three"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekTo(native(2))

            assertTrue(cursor.isValid())
            assertBytesEquals(array(3), cursor.transientKey())
            assertBytesEquals(array("three"), cursor.transientValue())

            cursor.next()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_04_Closed() {
        val cursor = ldb!!.newCursor()
        cursor.close()
        assertFailsWith<IllegalStateException> {
            cursor.seekTo(native(0))
        }
    }

    @Test
    fun test_05_SnapshotForward() {
        ldb!!.put(native(1), native("one"))
        ldb!!.put(native(3), native("three"))

        deferScope {
            val snapshot = ldb!!.newSnapshot().useInScope()
            ldb!!.put(native(2), native("three"))

            val cursor = ldb!!.newCursor(LevelDB.ReadOptions(snapshot = snapshot)).useInScope()
            cursor.seekToFirst()

            assertTrue(cursor.isValid())
            assertBytesEquals(array(1), cursor.transientKey())
            assertBytesEquals(array("one"), cursor.transientValue())

            cursor.next()

            assertTrue(cursor.isValid())
            assertBytesEquals(array(3), cursor.transientKey())
            assertBytesEquals(array("three"), cursor.transientValue())

            cursor.next()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_06_PutInside() {
        ldb!!.put(native("A"), native("A"))
        ldb!!.put(native("C"), native("C"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToFirst()
            assertTrue(cursor.isValid())
            assertBytesEquals(array("A"), cursor.transientKey())

            ldb!!.put(native("B"), native("B"), LevelDB.WriteOptions(sync = true))

            cursor.next()
            assertTrue(cursor.isValid())
            assertBytesEquals(array("C"), cursor.transientKey())

            cursor.next()
            assertFalse(cursor.isValid())
        }
    }

}
