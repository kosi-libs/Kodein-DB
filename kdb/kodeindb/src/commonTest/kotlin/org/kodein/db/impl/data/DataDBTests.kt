//package org.kodein.db.impl.data
//
//import kotlinx.io.core.String
//import kotlinx.io.core.readBytes
//import kotlinx.io.core.use
//import org.kodein.db.*
//import org.kodein.db.ascii.writeAscii
//import org.kodein.db.data.*
//import org.kodein.db.impl.utils.makeViewOf
//import org.kodein.db.leveldb.Allocation
//import org.kodein.db.test.utils.*
//import kotlin.test.*
//
//
//expect object DataDBTestFactory {
//    fun destroy()
//    fun open(): DataDB
//}
//
//class DataDBTests {
//
//    private var _ddb: DataDB? = null
//
//    private val _factory = DataDBTestFactory
//
//    @BeforeTest
//    fun setUp() {
//        _factory.destroy()
//        _ddb = _factory.open()
//    }
//
//    @AfterTest
//    fun tearDown() {
//        _ddb!!.close()
//        _ddb = null
//        _factory.destroy()
//    }
//
//    private fun indexSet(vararg nameValues: Any): Set<Index> {
//        val indexes = LinkedHashSet<Index>()
//        var i = 0
//        while (i < nameValues.size) {
//            indexes.add(Index(nameValues[i] as String, nameValues[i + 1] as Value))
//            i += 2
//        }
//        return indexes
//    }
//
//    private fun assertIteratorIs(version: Int, key: ByteArray, value: ByteArray, it: DataIterator) {
//        assertEquals(version, it.version())
//        assertBytesEquals(key, it.transientKey())
//        assertBytesEquals(value, it.transientValue())
//    }
//
//    private fun assertDBIs(vararg keyValues: ByteArray) {
//        _ddb!!.ldb.newCursor().use { cursor ->
//            cursor.seekToFirst()
//            var i = 0
//            while (cursor.isValid()) {
//                if (i >= keyValues.size)
//                    fail("DB contains additional entrie(s): " + cursor.transientKey().buffer.readBytes().description())
//                assertBytesEquals(keyValues[i], cursor.transientKey())
//                assertBytesEquals(keyValues[i + 1], cursor.transientValue())
//                cursor.next()
//                i += 2
//            }
//            if (i < keyValues.size)
//                fail("DB is missing entrie(s): " + String(keyValues[i]))
//        }
//    }
//
////    private fun assertDataIs(expectedVersion: Int, expectedBytes: ByteArray, actual: DataDB.Versioned) {
////        assertEquals(expectedVersion, actual.version)
////        assertBytesEquals(expectedBytes, actual.allocation)
////    }
//
//
//    @Test
//    fun test00_00_PutSimpleKeyWithoutIndex() {
////        Allocation.allocNativeBuffer(256).use { dst ->
////            dst.buffer.writeAscii("Coucou ")
////            val view = dst.buffer.makeViewOf { writeAscii("le monde") }
////            dst.buffer.writeAscii(" !")
////            assertBytesEquals(byteArray("le monde"), view)
////        }
//
//        _ddb!!.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"))
//
//        assertDBIs(
//                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 1, "ValueA1!")
//        )
//    }
//
//    @Test
//    fun test00_01_PutSimpleKeyWith1Index() {
//        _ddb!!.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta")))
//
//        assertDBIs(
//                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
//                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 1, "ValueA1!"),
//                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
//        )
//    }
//
//    @Test
//    fun test00_02_PutSimpleKeyWith2Index() {
//        _ddb!!.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta"), "Numbers", Value.ofAscii("forty", "two")))
//
//        assertDBIs(
//                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
//                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
//                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 1, "ValueA1!"),
//                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0, 0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0)
//        )
//    }
//
//    @Test
//    fun test00_03_PutTwiceWithRemovedIndex() {
//        _ddb!!.put("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta")))
//        _ddb!!.put("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB2!"))
//
//        assertDBIs(
//                byteArray('o', 0, "Test", 0, "aaa", 0, "bbb", 0), byteArray(0, 0, 0, 2, "ValueAB2!")
//        )
//    }
//
//    @Test
//    fun test00_04_PutTwiceWithDifferentIndex() {
//        _ddb!!.put("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta")))
//        _ddb!!.put("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB2!"), indexes = indexSet("Numbers", Value.ofAscii("forty", "two")))
//
//        assertDBIs(
//                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0, "bbb", 0), byteArray('o', 0, "Test", 0, "aaa", 0, "bbb", 0),
//                byteArray('o', 0, "Test", 0, "aaa", 0, "bbb", 0), byteArray(0, 0, 0, 2, "ValueAB2!"),
//                byteArray('r', 0, "Test", 0, "aaa", 0, "bbb", 0), byteArray(0, 0, 0, 33, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0, "bbb", 0)
//        )
//    }
//
//    @Test
//    fun test005_PutWithIncorrectVersion() {
//        val ex = assertFailsWith<DataConcurrentModificationException> {
//            _ddb!!.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta")), expectedVersion = 1)
//        }
//        assertEquals(1, ex.expectedVersion)
//        assertEquals(0, ex.actualVersion)
//        assertBytesEquals(byteArray('o', 0, "Test", 0, "aaa", 0), ex.key)
//        assertEquals(WriteType.PUT, ex.operationType)
//        assertEquals("Test", ex.type)
//
//        assertDBIs()
//    }
//
//    @Test
//    fun test00_06_PutTwiceWithIncorrectVersion() {
//        _ddb!!.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta")))
//
//        val ex = assertFailsWith<DataConcurrentModificationException> {
//            _ddb!!.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA2!"), indexes = indexSet("Numbers", Value.ofAscii("forty", "two")))
//        }
//
//        assertEquals(0, ex.expectedVersion)
//        assertEquals(1, ex.actualVersion)
//        assertBytesEquals(byteArray('o', 0, "Test", 0, "aaa", 0), ex.key)
//        assertEquals(WriteType.PUT, ex.operationType)
//        assertEquals("Test", ex.type)
//
//        assertDBIs(
//                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
//                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 1, "ValueA1!"),
//                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
//        )
//    }
//
//
////    @Test
////    fun test010_DeleteWithoutIndex() {
////        val vk = _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa", "bbb"), null, Value.ofAscii("ValueAB1"), -1, true)
////        val deleted = _ddb!!.Delete(MEM(), vk.key, -1, false, true)
////
////        assertNull(deleted)
////        assertDBIs(
////        )
////    }
////
////    @Test
////    fun test11_DeleteWithIndex() {
////        val vk = _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta"), "Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueA1!"), -1, true)
////        val deleted = _ddb!!.Delete(MEM(), vk.key, 1, false, true)
////
////        assertNull(deleted)
////        assertDBIs(
////        )
////    }
////
////    @Test
////    fun test012_DeleteUnknown() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        val deleted = _ddb!!.Delete(MEM(), DataKeys.getObjectKey("Test", Value.ofAscii("bbb"), false), -1, false, true)
////
////        assertNull(deleted)
////        assertDBIs(
////                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
////                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 1, "ValueA1!"),
////                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
////        )
////    }
////
////    @Test
////    fun test013_Delete1of2() {
////        val vk = _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        val deleted = _ddb!!.Delete(MEM(), vk.key, -1, false, true)
////
////        assertNull(deleted)
////        assertDBIs(
////                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0), byteArray('o', 0, "Test", 0, "bbb", 0),
////                byteArray('o', 0, "Test", 0, "bbb", 0), byteArray(0, 0, 0, 1, "ValueB1!"),
////                byteArray('r', 0, "Test", 0, "bbb", 0), byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0)
////        )
////    }
////
////    @Test
////    fun test014_DeleteWithIncorrectVersion() {
////        val vk = _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////
////        try {
////            _ddb!!.Delete(MEM(), vk.key, 2, false, true)
////            fail()
////        } catch (thrown: DataConcurrentModificationException) {
////            assertEquals(2, thrown.expectedVersion)
////            assertEquals(1, thrown.actualVersion)
////            assertBytesEquals(DataKeys.getObjectKey("Test", Value.ofAscii("aaa"), false), thrown.key)
////            assertEquals(Write.Type.DELETE, thrown.operationType)
////            assertEquals("Test", thrown.type)
////        }
////
////        assertDBIs(
////                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
////                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 1, "ValueA1!"),
////                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
////        )
////    }
////
////    @Test
////    fun test015_DeleteGet() {
////        val vk = _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa", "bbb"), null, Value.ofAscii("ValueAB1"), -1, true)
////        val deleted = _ddb!!.Delete(MEM(), vk.key, -1, true, true)
////
////        assertNotNull(deleted)
////        try {
////            assertEquals(1, deleted.version)
////            assertBytesEquals(byteArray("ValueAB1"), deleted.buffer())
////        } finally {
////            deleted.close()
////        }
////    }
////
////
////    @Test
////    fun test020_GetExisting() {
////        val vk = _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////
////        assertDataIs(1, byteArray("ValueA1!"), _ddb!!.Get(vk.key, null))
////        assertDataIs(1, byteArray("ValueB1!"), _ddb!!.Get(DataKeys.getObjectKey("Test", Value.ofAscii("bbb"), false), null))
////    }
////
////    @Test
////    fun test021_GetUnknownInEmptyDB() {
////        assertBytesEquals(null, _ddb!!.Get(DataKeys.getObjectKey("Test", Value.ofAscii("aaa"), false), null))
////    }
////
////    @Test
////    fun test022_GetUnknownInNonEmptyDB() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////
////        assertBytesEquals(null, _ddb!!.Get(DataKeys.getObjectKey("Test", Value.ofAscii("bbb"), false), null))
////    }
////
////    @Test
////    fun test030_FindByPKAll() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueB1!"), 0, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB2!"), 1, true)
////
////        val it = _ddb!!.FindAllByType("Test", null)
////        try {
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
////            assertBytesEquals(it.transientKey(), it.transientSeekKey())
////            it.Next()
////            assertTrue(it.Valid())
////            assertIteratorIs(2, byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB2!"), it)
////            assertBytesEquals(it.transientKey(), it.transientSeekKey())
////            it.Next()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test031_FindByPKAllReverse() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueB1!"), 0, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), 1, true)
////
////        val it = _ddb!!.FindAllByType("Test", null)
////        try {
////            assertTrue(it.Valid())
////            it.SeekToLast()
////            assertTrue(it.Valid())
////            assertIteratorIs(2, byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"), it)
////            assertBytesEquals(it.transientKey(), it.transientSeekKey())
////            it.Prev()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
////            assertBytesEquals(it.transientKey(), it.transientSeekKey())
////            it.Prev()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test032_FindByPKNothingInEmptyDB() {
////        val it = _ddb!!.FindAllByType("Test", null)
////        try {
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test033_FindByPKNothingInEmptyCollection() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////
////        val it = _ddb!!.FindAllByType("Yeah", null)
////        try {
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test034_FindByPKCompositeKey() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa", "a"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueAa1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa", "b"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueAb1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////
////        val it = _ddb!!.FindByPrimaryKeyPrefix("Test", Value.ofAscii("aaa"), false, null)
////        try {
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0, 'a', 0), byteArray("ValueAa1!"), it)
////            assertBytesEquals(it.transientKey(), it.transientSeekKey())
////            it.Next()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0, 'b', 0), byteArray("ValueAb1!"), it)
////            assertBytesEquals(it.transientKey(), it.transientSeekKey())
////            it.Next()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test035_FindByPKReverseCompositeKey() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa", "a"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueAa1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa", "b"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueAb1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////
////        val it = _ddb!!.FindByPrimaryKeyPrefix("Test", Value.ofAscii("aaa"), false, null)
////        try {
////            assertTrue(it.Valid())
////            it.SeekToLast()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0, 'b', 0), byteArray("ValueAb1!"), it)
////            assertBytesEquals(it.transientKey(), it.transientSeekKey())
////            it.Prev()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0, 'a', 0), byteArray("ValueAa1!"), it)
////            assertBytesEquals(it.transientKey(), it.transientSeekKey())
////            it.Prev()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test036_FindByPKUnknownKey() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////
////        val it = _ddb!!.FindByPrimaryKeyPrefix("Test", Value.ofAscii("ccc"), false, null)
////        try {
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test040_SeekPK() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////
////        val it = _ddb!!.FindAllByType("Test", null)
////        try {
////            assertTrue(it.Valid())
////            it.Seek(DataKeys.getObjectKey("Test", Value.ofAscii("bba"), false))
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"), it)
////            it.Next()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test041_SeekPKBefore() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////
////        val it = _ddb!!.FindAllByType("Test", null)
////        try {
////            assertTrue(it.Valid())
////            it.Seek(DataKeys.getObjectKey("Test", Value.ofAscii("A"), false))
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test042_SeekPKAfter() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////
////        val it = _ddb!!.FindAllByType("Test", null)
////        try {
////            assertTrue(it.Valid())
////            it.Seek(DataKeys.getObjectKey("Test", Value.ofAscii("z"), false))
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test050_FindByIndexAll() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueC1!"), 0, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC2!"), 1, true)
////
////        val it = _ddb!!.FindAllByIndex("Test", "Symbols", null)
////        try {
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
////            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
////            it.Next()
////            assertTrue(it.Valid())
////            assertIteratorIs(2, byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC2!"), it)
////            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), it.transientSeekKey())
////            it.Next()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test051_FindByIndexReverse() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueC1!"), 0, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC2!"), 1, true)
////
////        val it = _ddb!!.FindAllByIndex("Test", "Symbols", null)
////        try {
////            assertTrue(it.Valid())
////            it.SeekToLast()
////            assertTrue(it.Valid())
////            assertIteratorIs(2, byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC2!"), it)
////            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), it.transientSeekKey())
////            it.Prev()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
////            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
////            it.Prev()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test052_FindByIndexNothingInEmptyDB() {
////        val it = _ddb!!.FindAllByIndex("Test", "Symbols", null)
////        try {
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test053_FindByIndexNothingInEmptyCollection() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC1!"), -1, true)
////
////        val it = _ddb!!.FindAllByIndex("Yeah", "Symbols", null)
////        try {
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test054_FindByIndexNothingInEmptyIndex() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC1!"), -1, true)
////
////        val it = _ddb!!.FindAllByIndex("Test", "Names", null)
////        try {
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test055_FindByIndexComposite() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC1!"), 0, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("alpha", "gamma")), Value.ofAscii("ValueC2!"), 1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ddd"), indexSet("Symbols", Value.ofAscii("delta", "gamma")), Value.ofAscii("ValueE1!"), -1, true)
////
////        val it = _ddb!!.FindByIndexPrefix("Test", "Symbols", Value.ofAscii("alpha"), false, null)
////        try {
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
////            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
////            it.Next()
////            assertTrue(it.Valid())
////            assertIteratorIs(2, byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC2!"), it)
////            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "gamma", 0, "ccc", 0), it.transientSeekKey())
////            it.Next()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test056_FindByIndexReverseComposite() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC1!"), 0, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("alpha", "gamma")), Value.ofAscii("ValueC2!"), 1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ddd"), indexSet("Symbols", Value.ofAscii("delta", "gamma")), Value.ofAscii("ValueE1!"), -1, true)
////
////        val it = _ddb!!.FindByIndexPrefix("Test", "Symbols", Value.ofAscii("alpha"), false, null)
////        try {
////            assertTrue(it.Valid())
////            it.SeekToLast()
////            assertTrue(it.Valid())
////            assertIteratorIs(2, byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC2!"), it)
////            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "gamma", 0, "ccc", 0), it.transientSeekKey())
////            it.Prev()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
////            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
////            it.Prev()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test057_FindByIndexCompositeUnknown() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("alpha", "gamma")), Value.ofAscii("ValueC1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ddd"), indexSet("Symbols", Value.ofAscii("delta", "gamma")), Value.ofAscii("ValueE1!"), -1, true)
////
////        val it = _ddb!!.FindByIndexPrefix("Test", "Symbols", Value.ofAscii("gamma"), false, null)
////        try {
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test058_FindByEmptyIndex() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("index", Value.ofAscii("value")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("index", Value.ofAscii("")), Value.ofAscii("ValueB1!"), -1, true)
////
////        val it = _ddb!!.FindByIndexPrefix("Test", "index", Value.ofAscii(""), false, null)
////        try {
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"), it)
////
////            it.Next()
////
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////
////    }
////
////    @Test
////    fun test060_SeekIndex() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC1!"), -1, true)
////
////        val it = _ddb!!.FindAllByIndex("Test", "Symbols", null)
////        try {
////            assertTrue(it.Valid())
////            it.Seek(byteBuffer('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0))
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC1!"), it)
////            it.Next()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test061_SeekIndexBefore() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC1!"), -1, true)
////
////        val it = _ddb!!.FindAllByIndex("Test", "Symbols", null)
////        try {
////            assertTrue(it.Valid())
////            it.Seek(byteBuffer('i', 0, "Test", 0, "Symbols", 0, "A", 0, "A", 0))
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test062_SeekIndexAfter() {
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC1!"), -1, true)
////
////        val it = _ddb!!.FindAllByIndex("Test", "Symbols", null)
////        try {
////            assertTrue(it.Valid())
////            it.Seek(byteBuffer('i', 0, "Test", 0, "Symbols", 0, "z", 0, "z", 0))
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test070_FindAll() {
////        _ddb!!.Put(MEM(), "Test1", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA!"), -1, true)
////        _ddb!!.Put(MEM(), "Test1", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB!"), -1, true)
////        _ddb!!.Put(MEM(), "Test2", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC!"), -1, true)
////
////        val it = _ddb!!.FindAll(null)
////        try {
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test1", 0, "aaa", 0), byteArray("ValueA!"), it)
////            it.Next()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test1", 0, "bbb", 0), byteArray("ValueB!"), it)
////            it.Next()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test2", 0, "ccc", 0), byteArray("ValueC!"), it)
////            it.Next()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test071_FindAllReverse() {
////        _ddb!!.Put(MEM(), "Test1", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA!"), -1, true)
////        _ddb!!.Put(MEM(), "Test1", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB!"), -1, true)
////        _ddb!!.Put(MEM(), "Test2", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC!"), -1, true)
////
////        val it = _ddb!!.FindAll(null)
////        try {
////            it.SeekToLast()
////
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test2", 0, "ccc", 0), byteArray("ValueC!"), it)
////            it.Prev()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test1", 0, "bbb", 0), byteArray("ValueB!"), it)
////            it.Prev()
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test1", 0, "aaa", 0), byteArray("ValueA!"), it)
////            it.Prev()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test080_BatchPut() {
////
////        val batch = DataBatch(MEM(), _ddb, OptionMap(Write.SYNC))
////        val operations = Batched()
////        operations.add(batch.PutOperation("Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1))
////        operations.add(batch.PutOperation("Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1))
////
////        assertDBIs()
////
////        operations.Execute(batch)
////
////        assertDBIs(
////                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0), byteArray('o', 0, "Test", 0, "bbb", 0),
////                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
////                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 1, "ValueA1!"),
////                byteArray('o', 0, "Test", 0, "bbb", 0), byteArray(0, 0, 0, 1, "ValueB1!"),
////                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0),
////                byteArray('r', 0, "Test", 0, "bbb", 0), byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0)
////        )
////    }
////
////    @Test
////    fun test081_BatchDelete() {
////
////        val vk = _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")), Value.ofAscii("ValueB1!"), -1, true)
////        _ddb!!.Put(MEM(), "Test", Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")), Value.ofAscii("ValueC1!"), -1, true)
////
////        val batch = DataBatch(MEM(), _ddb, OptionMap(Write.SYNC))
////        val operations = Batched()
////        operations.add(batch.DeleteOperation(vk.key, -1))
////        operations.add(batch.DeleteOperation(DataKeys.getObjectKey("Test", Value.ofAscii("bbb"), false), -1))
////
////        assertDBIs(
////                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0), byteArray('o', 0, "Test", 0, "bbb", 0),
////                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
////                byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), byteArray('o', 0, "Test", 0, "ccc", 0),
////                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 1, "ValueA1!"),
////                byteArray('o', 0, "Test", 0, "bbb", 0), byteArray(0, 0, 0, 1, "ValueB1!"),
////                byteArray('o', 0, "Test", 0, "ccc", 0), byteArray(0, 0, 0, 1, "ValueC1!"),
////                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0),
////                byteArray('r', 0, "Test", 0, "bbb", 0), byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0),
////                byteArray('r', 0, "Test", 0, "ccc", 0), byteArray(0, 0, 0, 31, 'i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0)
////        )
////
////        operations.Execute(batch)
////
////        assertDBIs(
////                byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), byteArray('o', 0, "Test", 0, "ccc", 0),
////                byteArray('o', 0, "Test", 0, "ccc", 0), byteArray(0, 0, 0, 1, "ValueC1!"),
////                byteArray('r', 0, "Test", 0, "ccc", 0), byteArray(0, 0, 0, 31, 'i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0)
////        )
////    }
////
////    @Test
////    fun test090_PutCloseOpenGet() {
////        val res = _ddb!!.Put(MEM(), "Test", Value.ofAscii("key"), null, Value.ofAscii("value"), -1, true)
////
////        _ddb!!.close()
////
////        _ddb = _factory.open()
////
////        assertBytesEquals(byteArray("value"), _ddb!!.Get(res.key, null))
////    }
////
////    @Test
////    fun test091_PutCloseOpenIter() {
////        val res = _ddb!!.Put(MEM(), "Test", Value.ofAscii("key"), null, Value.ofAscii("value"), -1, true)
////
////        _ddb!!.close()
////
////        _ddb = _factory.open()
////
////        val it = _ddb!!.FindAllByType("Test", null)
////        try {
////            assertTrue(it.Valid())
////            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "key", 0), byteArray("value"), it)
////
////            it.Next()
////            assertFalse(it.Valid())
////        } finally {
////            it.close()
////        }
////    }
////
////    @Test
////    fun test100_FindIndexes() {
////        val key = _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Numbers", Value.ofAscii("forty", "two"), "Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA!"), -1, true).key
////
////        val indexes = _ddb!!.findIndexes(key)
////
////        assertEquals(2, indexes.size.toLong())
////        assertTrue(indexes.contains("Numbers"))
////        assertTrue(indexes.contains("Symbols"))
////    }
////
////    @Test
////    fun test101_FindNoIndexes() {
////        val key = _ddb!!.Put(MEM(), "Test", Value.ofAscii("aaa"), null, Value.ofAscii("ValueA!"), -1, true).key
////
////        val indexes = _ddb!!.findIndexes(key)
////
////        assertTrue(indexes.isEmpty())
////    }
////
////    @Test
////    fun test102_FindUnknownIndexes() {
////        val indexes = _ddb!!.findIndexes(DataKeys.getObjectKey("Unknown", Value.ofAscii("A"), false))
////
////        assertTrue(indexes.isEmpty())
////    }
////
////    @Test
////    fun test110_MultiThreadPut() {
////
////        val COUNT = 100
////
////        val executor = Executors.newFixedThreadPool(5)
////        val count = AtomicInteger(0)
////
////        val run = object : Runnable {
////            override fun run() {
////                val it = _ddb!!.FindAllByIndex("Test", "index", null)
////                try {
////                    while (it.Valid()) {
////                        //                        it.value();
////                        it.Next()
////                    }
////                } finally {
////                    it.close()
////                }
////                count.incrementAndGet()
////            }
////        }
////
////        for (i in 0 until COUNT) {
////            _ddb!!.Put(MEM(), "Test", Value.ofAscii(UUID.randomUUID().toString()), indexSet("index", Value.ofAscii(UUID.randomUUID().toString())), Value.ofAscii(UUID.randomUUID().toString()), -1, true)
////            executor.submit(run)
////        }
////
////        executor.shutdown()
////        try {
////            executor.awaitTermination(20, TimeUnit.SECONDS)
////        } catch (e: InterruptedException) {
////            throw RuntimeException(e)
////        }
////
////        assertEquals(COUNT.toLong(), count.get().toLong())
////    }
////
////    @Test
////    fun test111_MultiThreadDelete() {
////
////        val COUNT = 100
////
////        val keys = ArrayList<ByteBuffer>(COUNT)
////        for (i in 0 until COUNT)
////            keys.add(_ddb!!.Put(MEM(), "Test", Value.ofAscii(UUID.randomUUID().toString()), indexSet("index", Value.ofAscii(UUID.randomUUID().toString())), Value.ofAscii(UUID.randomUUID().toString()), -1, true).key)
////
////        val executor = Executors.newFixedThreadPool(5)
////        val count = AtomicInteger(0)
////
////        val run = object : Runnable {
////            override fun run() {
////                val it = _ddb!!.FindAllByIndex("Test", "index", null)
////                try {
////                    while (it.Valid()) {
////                        //                        it.value();
////                        it.Next()
////                    }
////                } catch (t: Throwable) {
////                    t.printStackTrace()
////                    fail(t.message)
////                    throw RuntimeException(t)
////                } finally {
////                    it.close()
////                }
////                count.incrementAndGet()
////            }
////        }
////
////        for (key in keys) {
////            _ddb!!.Delete(MEM(), key, -1, false, false)
////            executor.submit(run)
////        }
////
////        executor.shutdown()
////        try {
////            executor.awaitTermination(20, TimeUnit.SECONDS)
////        } catch (e: InterruptedException) {
////            throw RuntimeException(e)
////        }
////
////        assertEquals(COUNT.toLong(), count.get().toLong())
////    }
//
//}