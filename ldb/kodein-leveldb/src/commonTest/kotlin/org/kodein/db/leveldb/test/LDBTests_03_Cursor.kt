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
import org.kodein.memory.util.deferScope
import kotlin.test.*

@Suppress("ClassName")
abstract class LDBTests_03_Cursor : LevelDBTests() {

    class LDB : LDBTests_03_Cursor() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_03_Cursor() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_Forward() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(2), buffer("two"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToFirst()

            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray(1), cursor.transientKey())
            assertBytesEquals(byteArray("one"), cursor.transientValue())

            cursor.next()

            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray(2), cursor.transientKey())
            assertBytesEquals(byteArray("two"), cursor.transientValue())

            cursor.next()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_01_Backward() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(2), buffer("two"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToLast()

            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray(2), cursor.transientKey())
            assertBytesEquals(byteArray("two"), cursor.transientValue())

            cursor.prev()

            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray(1), cursor.transientKey())
            assertBytesEquals(byteArray("one"), cursor.transientValue())

            cursor.prev()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_02_Seek() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(3), buffer("three"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekTo(buffer(2))

            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray(3), cursor.transientKey())
            assertBytesEquals(byteArray("three"), cursor.transientValue())

            cursor.next()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_03_DirectSeek() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(3), buffer("three"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekTo(buffer(2))

            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray(3), cursor.transientKey())
            assertBytesEquals(byteArray("three"), cursor.transientValue())

            cursor.next()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_04_Closed() {
        val cursor = ldb!!.newCursor()
        cursor.close()
        assertFailsWith<IllegalStateException> {
            cursor.seekTo(buffer(0))
        }
    }

    @Test
    fun test_05_SnapshotForward() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(3), buffer("three"))

        deferScope {
            val snapshot = ldb!!.newSnapshot().useInScope()
            ldb!!.put(buffer(2), buffer("three"))

            val cursor = ldb!!.newCursor(LevelDB.ReadOptions(snapshot = snapshot)).useInScope()
            cursor.seekToFirst()

            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray(1), cursor.transientKey())
            assertBytesEquals(byteArray("one"), cursor.transientValue())

            cursor.next()

            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray(3), cursor.transientKey())
            assertBytesEquals(byteArray("three"), cursor.transientValue())

            cursor.next()

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_06_PutInside() {
        ldb!!.put(buffer("A"), buffer("A"))
        ldb!!.put(buffer("C"), buffer("C"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToFirst()
            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray("A"), cursor.transientKey())

            ldb!!.put(buffer("B"), buffer("B"), LevelDB.WriteOptions(sync = true))

            cursor.next()
            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray("C"), cursor.transientKey())

            cursor.next()
            assertFalse(cursor.isValid())
        }
    }

}
