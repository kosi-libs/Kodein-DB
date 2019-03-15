package org.kodein.db.leveldb.test

import kotlinx.io.core.use
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import kotlin.test.*

class LDBTests_03_Cursor : LevelDBTests() {

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

    @Test()
    fun test_04_Closed() {
        val cursor = ldb!!.newCursor()
        cursor.close()
        assertFailsWith<IllegalStateException> {
            cursor.seekTo(buffer(0))
        }
    }

    @Test
    fun test_05_Array() {
        ldb!!.put(buffer("key0"), buffer("value0"))
        ldb!!.put(buffer("key1"), buffer("value1"))

        ldb!!.close()

        ldb = factory.open("db", options().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToFirst()
            assertTrue(cursor.isValid())

            cursor.nextArray(10).use { array ->
                assertEquals(2, array.size)
                assertBytesEquals(byteArray("key0"), array.getKey(0))
                assertBytesEquals(byteArray("value0"), array.getValue(0))
                assertBytesEquals(byteArray("key1"), array.getKey(1))
                assertBytesEquals(byteArray("value1"), array.getValue(1))
            }

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_06_ArrayFull() {
        ldb!!.put(buffer("key0"), buffer("value0"))
        ldb!!.put(buffer("key1"), buffer("value1"))

        ldb!!.close()

        ldb = factory.open("db", options().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToFirst()
            assertTrue(cursor.isValid())

            cursor.nextArray(2, 4).use { array ->
                assertEquals(2, array.size)
                assertBytesEquals(byteArray("key0"), array.getKey(0))
                assertBytesEquals(byteArray("value0"), array.getValue(0))
                assertBytesEquals(byteArray("key1"), array.getKey(1))
                assertBytesEquals(byteArray("value1"), array.getValue(1))
            }

            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test_07_SnapshotForward() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(3), buffer("three"))

        ldb!!.newSnapshot().use { snapshot ->
            ldb!!.put(buffer(2), buffer("three"))

            ldb!!.newCursor(LevelDB.ReadOptions(snapshot = snapshot)).use { cursor ->
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
    }

    @Test
    fun test_08_IndirectValue() {
        ldb!!.put(buffer("one"), buffer("two"))
        ldb!!.put(buffer("two"), buffer("three"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekTo(buffer("one"))
            assertTrue(cursor.isValid())

            val value = ldb!!.indirectGet(cursor)!!
            assertBytesEquals(byteArray("three"), value)
            value.close()
        }
    }

    @Test
    fun test_09_IndirectUnexistingValue() {
        ldb!!.put(buffer("one"), buffer("two"))

        ldb!!.newCursor().use { cursor ->
            cursor.seekTo(buffer("one"))
            assertTrue(cursor.isValid())

            assertNull(ldb!!.indirectGet(cursor))
            assertNull(ldb!!.indirectGet(cursor, LevelDB.ReadOptions()))
        }
    }

    @Test
    fun test_10_PutInside() {
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
