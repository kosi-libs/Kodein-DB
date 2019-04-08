package org.kodein.db.impl.data

import kotlinx.io.core.String
import kotlinx.io.core.readBytes
import kotlinx.io.core.use
import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataIterator
import org.kodein.db.leveldb.Allocation
import org.kodein.db.test.utils.AbstractTests
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.db.test.utils.description
import kotlin.test.*


expect object DataDBTestFactory {
    fun destroy()
    fun open(): DataDB
}

class DataDBTests : AbstractTests() {

    private var _ddb: DataDB? = null
    
    private val ddb: DataDB get() = _ddb!!

    private val _factory = DataDBTestFactory

    @BeforeTest
    fun setUp() {
        _factory.destroy()
        _ddb = _factory.open()
    }

    @AfterTest
    fun tearDown() {
        ddb.close()
        _ddb = null
        _factory.destroy()
    }

    private fun indexSet(vararg nameValues: Any): Set<Index> {
        val indexes = LinkedHashSet<Index>()
        var i = 0
        while (i < nameValues.size) {
            indexes.add(Index(nameValues[i] as String, nameValues[i + 1] as Value))
            i += 2
        }
        return indexes
    }

    private fun assertIteratorIs(key: ByteArray, value: ByteArray, it: DataIterator) {
        assertBytesEquals(key, it.transientKey())
        assertBytesEquals(value, it.transientValue())
    }

    private fun assertDBIs(vararg keyValues: ByteArray) {
        (ddb as DataDBImpl).ldb.newCursor().use { cursor ->
            cursor.seekToFirst()
            var i = 0
            while (cursor.isValid()) {
                if (i >= keyValues.size)
                    fail("DB contains additional entrie(s): " + cursor.transientKey().buffer.readBytes().description())
                assertBytesEquals(keyValues[i], cursor.transientKey())
                assertBytesEquals(keyValues[i + 1], cursor.transientValue())
                cursor.next()
                i += 2
            }
            if (i < keyValues.size)
                fail("DB is missing entrie(s): " + String(keyValues[i]))
        }
    }

    private fun assertDataIs(expectedBytes: ByteArray, actual: Allocation?) {
        assertNotNull(actual)
        actual.use {
            assertBytesEquals(expectedBytes, actual)
        }
    }


    @Test
    fun test00_00_PutSimpleKeyWithoutIndex() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"))

        assertDBIs(
                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!")
        )
    }

    @Test
    fun test00_01_PutSimpleKeyWith1Index() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta")))

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"),
                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
        )
    }

    @Test
    fun test00_02_PutSimpleKeyWith2Index() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta"), "Numbers", Value.ofAscii("forty", "two")))

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"),
                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0, 0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0)
        )
    }

    @Test
    fun test00_03_PutTwiceWithRemovedIndex() {
        ddb.put("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB2!"))

        assertDBIs(
                byteArray('o', 0, "Test", 0, "aaa", 0, "bbb", 0), byteArray("ValueAB2!")
        )
    }

    @Test
    fun test00_04_PutTwiceWithDifferentIndex() {
        ddb.put("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB1!"), indexes = indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB2!"), indexes = indexSet("Numbers", Value.ofAscii("forty", "two")))

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0, "bbb", 0), byteArray('o', 0, "Test", 0, "aaa", 0, "bbb", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0, "bbb", 0), byteArray("ValueAB2!"),
                byteArray('r', 0, "Test", 0, "aaa", 0, "bbb", 0), byteArray(0, 0, 0, 33, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0, "bbb", 0)
        )
    }

    @Test
    fun test010_DeleteWithoutIndex() {
        ddb.putAndGetKey("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB1")).use {
            ddb.delete(it.key)
        }

        assertDBIs(
        )
    }

    @Test
    fun test11_DeleteWithIndex() {
        ddb.putAndGetKey("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta"), "Numbers", Value.ofAscii("forty", "two"))).use {
            ddb.delete(it.key)
        }

        assertDBIs(
        )
    }

    @Test
    fun test012_DeleteUnknown() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.allocKey("Test", Value.ofAscii("bbb")).use {
            ddb.delete(it)
        }

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"),
                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
        )
    }

    @Test
    fun test013_Delete1of2() {
        ddb.putAndGetKey("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta"))).use {
            ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
            ddb.delete(it.key)
        }

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0), byteArray('o', 0, "Test", 0, "bbb", 0),
                byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"),
                byteArray('r', 0, "Test", 0, "bbb", 0), byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0)
        )
    }

    @Test
    fun test020_GetExisting() {
        ddb.putAndGetKey("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta"))).use {
            ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

            assertDataIs(byteArray("ValueA1!"), ddb.get(it.key))
        }

        ddb.allocKey("Test", Value.ofAscii("bbb")).use {
            assertDataIs(byteArray("ValueB1!"), ddb.get(it))
        }
    }

    @Test
    fun test021_GetUnknownInEmptyDB() {
        ddb.allocKey("Test", Value.ofAscii("aaa")).use {
            assertNull(ddb.get(it))
        }
    }

    @Test
    fun test022_GetUnknownInNonEmptyDB() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))

        ddb.allocKey("Test", Value.ofAscii("bbb")).use {
            assertNull(ddb.get(it))
        }
    }

    @Test
    fun test030_FindByTypeAll() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB2!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB2!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test031_FindByPKAllReverse() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test032_FindByPKNothingInEmptyDB() {
        ddb.findAllByType("Test").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test033_FindByPKNothingInEmptyCollection() {
        ddb.put("Test", Value.ofAscii("ValueA1!"), Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("ValueB1!"), Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        ddb.findAllByType("Yeah").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test034_FindByPKCompositeKey() {
        ddb.put("Test", Value.ofAscii("aaa", "a"), Value.ofAscii("ValueAa1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("aaa", "b"), Value.ofAscii("ValueAb1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        ddb.findByPrimaryKeyPrefix("Test", Value.ofAscii("aaa")).use {
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0, 'a', 0), byteArray("ValueAa1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0, 'b', 0), byteArray("ValueAb1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test035_FindByPKReverseCompositeKey() {
        ddb.put("Test", Value.ofAscii("aaa", "a"), Value.ofAscii("ValueAa1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("aaa", "b"), Value.ofAscii("ValueAb1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        ddb.findByPrimaryKeyPrefix("Test", Value.ofAscii("aaa")).use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0, 'b', 0), byteArray("ValueAb1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0, 'a', 0), byteArray("ValueAa1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test036_FindByPKUnknownKey() {
        ddb.put( "Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put( "Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        ddb.findByPrimaryKeyPrefix("Test", Value.ofAscii("ccc")).use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test040_SeekPK() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            ddb.allocKey("Test", Value.ofAscii("bba")).use { key ->
                it.seekTo(key)
            }
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test041_SeekPKBefore() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            ddb.allocKey("Test", Value.ofAscii("A")).use { key ->
                it.seekTo(key)
            }
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
        }
    }

    @Test
    fun test042_SeekPKAfter() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            ddb.allocKey("Test", Value.ofAscii("z")).use { key ->
                it.seekTo(key)
            }
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test050_FindByIndexAll() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC2!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex("Test", "Symbols").use {
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC2!"), it)
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test051_FindByIndexReverse() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC2!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex("Test", "Symbols").use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC2!"), it)
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test052_FindByIndexNothingInEmptyDB() {
        ddb.findAllByIndex("Test", "Symbols").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test053_FindByIndexNothingInEmptyCollection() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex("Yeah", "Symbols").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test054_FindByIndexNothingInEmptyIndex() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex("Test", "Names").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test055_FindByIndexComposite() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC2!"), indexSet("Symbols", Value.ofAscii("alpha", "gamma")))
        ddb.put("Test", Value.ofAscii("ddd"), Value.ofAscii("ValueE1!"), indexSet("Symbols", Value.ofAscii("delta", "gamma")))

        ddb.findByIndexPrefix("Test", "Symbols", Value.ofAscii("alpha")).use {
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC2!"), it)
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "gamma", 0, "ccc", 0), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test056_FindByIndexReverseComposite() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC2!"), indexSet("Symbols", Value.ofAscii("alpha", "gamma")))
        ddb.put("Test", Value.ofAscii("ddd"), Value.ofAscii("ValueE1!"), indexSet("Symbols", Value.ofAscii("delta", "gamma")))

        ddb.findByIndexPrefix("Test", "Symbols", Value.ofAscii("alpha")).use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC2!"), it)
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "gamma", 0, "ccc", 0), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test057_FindByIndexCompositeUnknown() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("alpha", "gamma")))
        ddb.put("Test", Value.ofAscii("ddd"), Value.ofAscii("ValueE1!"), indexSet("Symbols", Value.ofAscii("delta", "gamma")))

        ddb.findByIndexPrefix("Test", "Symbols", Value.ofAscii("gamma")).use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test058_FindByEmptyIndex() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("index", Value.ofAscii("value")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("index", Value.ofAscii("")))

        ddb.findByIndexPrefix("Test", "index", Value.ofAscii("")).use {
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"), it)

            it.next()

            assertFalse(it.isValid())
        }

    }

    @Test
    fun test060_SeekIndex() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex("Test", "Symbols").use {
            assertTrue(it.isValid())
            it.seekTo(buffer('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0))
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC1!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test061_SeekIndexBefore() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex("Test", "Symbols").use {
            assertTrue(it.isValid())
            it.seekTo(buffer('i', 0, "Test", 0, "Symbols", 0, "A", 0, "A", 0))
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
        }
    }

    @Test
    fun test062_SeekIndexAfter() {
        ddb.put("Test", Value.ofAscii("ValueA1!"), Value.ofAscii("aaa"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("ValueB1!"), Value.ofAscii("bbb"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ValueC1!"), Value.ofAscii("ccc"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex("Test", "Symbols").use {
            assertTrue(it.isValid())
            it.seekTo(buffer('i', 0, "Test", 0, "Symbols", 0, "z", 0, "z", 0))
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test070_FindAll() {
        ddb.put("Test1", Value.ofAscii("aaa"), Value.ofAscii("ValueA!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test1", Value.ofAscii("bbb"), Value.ofAscii("ValueB!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test2", Value.ofAscii("ccc"), Value.ofAscii("ValueC!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        ddb.findAll().use {
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test1", 0, "aaa", 0), byteArray("ValueA!"), it)
            it.next()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test1", 0, "bbb", 0), byteArray("ValueB!"), it)
            it.next()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test2", 0, "ccc", 0), byteArray("ValueC!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test071_FindAllReverse() {
        ddb.put("Test1", Value.ofAscii("aaa"), Value.ofAscii("ValueA!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test1", Value.ofAscii("bbb"), Value.ofAscii("ValueB!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test2", Value.ofAscii("ccc"), Value.ofAscii("ValueC!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        ddb.findAll().use {
            it.seekToLast()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test2", 0, "ccc", 0), byteArray("ValueC!"), it)
            it.prev()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test1", 0, "bbb", 0), byteArray("ValueB!"), it)
            it.prev()
            assertTrue(it.isValid())
            assertIteratorIs(byteArray('o', 0, "Test1", 0, "aaa", 0), byteArray("ValueA!"), it)
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test080_BatchPut() {

        val batch = ddb.newBatch()
        batch.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        batch.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        assertDBIs()

        batch.write()

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0), byteArray('o', 0, "Test", 0, "bbb", 0),
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"),
                byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"),
                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0),
                byteArray('r', 0, "Test", 0, "bbb", 0), byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0)
        )
    }

    @Test
    fun test081_BatchDelete() {

        val vk = ddb.putAndGetKey("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers", Value.ofAscii("forty", "two")))
        ddb.put("Test", Value.ofAscii("ccc"), Value.ofAscii("ValueC1!"), indexSet("Symbols", Value.ofAscii("gamma", "delta")))

        val batch = ddb.newBatch()
        batch.delete(vk.key)
        ddb.allocKey("Test", Value.ofAscii("bbb")).use {
            batch.delete(it)
        }

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0), byteArray('o', 0, "Test", 0, "bbb", 0),
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), byteArray('o', 0, "Test", 0, "ccc", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"),
                byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"),
                byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC1!"),
                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0),
                byteArray('r', 0, "Test", 0, "bbb", 0), byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0),
                byteArray('r', 0, "Test", 0, "ccc", 0), byteArray(0, 0, 0, 31, 'i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0)
        )

        batch.write()

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), byteArray('o', 0, "Test", 0, "ccc", 0),
                byteArray('o', 0, "Test", 0, "ccc", 0), byteArray("ValueC1!"),
                byteArray('r', 0, "Test", 0, "ccc", 0), byteArray(0, 0, 0, 31, 'i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0)
        )
    }

    @Test
    fun test082_BatchOverride() {

        val batch = ddb.newBatch()
        batch.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueBatch!"), indexSet("Symbols", Value.ofAscii("alpha", "beta")))

        assertDBIs()

        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValuePut!"), indexSet("Numbers", Value.ofAscii("forty", "two")))

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValuePut!"),
                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0)
        )

        batch.write()

        assertDBIs(
//                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueBatch!"),
                byteArray('r', 0, "Test", 0, "aaa", 0), byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
        )
    }


//    @Test
//    fun test090_PutCloseOpenGet() {
//        val res = ddb.Put(MEM(), "Test", Value.ofAscii("key"), null, Value.ofAscii("value"), -1, true)
//
//        ddb.close()
//
//        _ddb = _factory.open()
//
//        assertBytesEquals(byteArray("value"), ddb.Get(res.key, null))
//    }
//
//    @Test
//    fun test091_PutCloseOpenIter() {
//        val res = ddb.Put(MEM(), "Test", Value.ofAscii("key"), null, Value.ofAscii("value"), -1, true)
//
//        ddb.close()
//
//        _ddb = _factory.open()
//
//        val it = ddb.FindAllByType("Test", null)
//        try {
//            assertTrue(it.Valid())
//            assertIteratorIs(1, byteArray('o', 0, "Test", 0, "key", 0), byteArray("value"), it)
//
//            it.Next()
//            assertFalse(it.Valid())
//        } finally {
//            it.close()
//        }
//    }
//
//    @Test
//    fun test100_FindIndexes() {
//        val key = ddb.Put(MEM(), "Test", Value.ofAscii("aaa"), indexSet("Numbers", Value.ofAscii("forty", "two"), "Symbols", Value.ofAscii("alpha", "beta")), Value.ofAscii("ValueA!"), -1, true).key
//
//        val indexes = ddb.findIndexes(key)
//
//        assertEquals(2, indexes.size.toLong())
//        assertTrue(indexes.contains("Numbers"))
//        assertTrue(indexes.contains("Symbols"))
//    }
//
//    @Test
//    fun test101_FindNoIndexes() {
//        val key = ddb.Put(MEM(), "Test", Value.ofAscii("aaa"), null, Value.ofAscii("ValueA!"), -1, true).key
//
//        val indexes = ddb.findIndexes(key)
//
//        assertTrue(indexes.isEmpty())
//    }
//
//    @Test
//    fun test102_FindUnknownIndexes() {
//        val indexes = ddb.findIndexes(DataKeys.getObjectKey("Unknown", Value.ofAscii("A"), false))
//
//        assertTrue(indexes.isEmpty())
//    }
//
//    @Test
//    fun test110_MultiThreadPut() {
//
//        val COUNT = 100
//
//        val executor = Executors.newFixedThreadPool(5)
//        val count = AtomicInteger(0)
//
//        val run = object : Runnable {
//            override fun run() {
//                val it = ddb.FindAllByIndex("Test", "index", null)
//                try {
//                    while (it.Valid()) {
//                        //                        it.value();
//                        it.Next()
//                    }
//                } finally {
//                    it.close()
//                }
//                count.incrementAndGet()
//            }
//        }
//
//        for (i in 0 until COUNT) {
//            ddb.Put(MEM(), "Test", Value.ofAscii(UUID.randomUUID().toString()), indexSet("index", Value.ofAscii(UUID.randomUUID().toString())), Value.ofAscii(UUID.randomUUID().toString()), -1, true)
//            executor.submit(run)
//        }
//
//        executor.shutdown()
//        try {
//            executor.awaitTermination(20, TimeUnit.SECONDS)
//        } catch (e: InterruptedException) {
//            throw RuntimeException(e)
//        }
//
//        assertEquals(COUNT.toLong(), count.get().toLong())
//    }
//
//    @Test
//    fun test111_MultiThreadDelete() {
//
//        val COUNT = 100
//
//        val keys = ArrayList<ByteBuffer>(COUNT)
//        for (i in 0 until COUNT)
//            keys.add(ddb.Put(MEM(), "Test", Value.ofAscii(UUID.randomUUID().toString()), indexSet("index", Value.ofAscii(UUID.randomUUID().toString())), Value.ofAscii(UUID.randomUUID().toString()), -1, true).key)
//
//        val executor = Executors.newFixedThreadPool(5)
//        val count = AtomicInteger(0)
//
//        val run = object : Runnable {
//            override fun run() {
//                val it = ddb.FindAllByIndex("Test", "index", null)
//                try {
//                    while (it.Valid()) {
//                        //                        it.value();
//                        it.Next()
//                    }
//                } catch (t: Throwable) {
//                    t.printStackTrace()
//                    fail(t.message)
//                    throw RuntimeException(t)
//                } finally {
//                    it.close()
//                }
//                count.incrementAndGet()
//            }
//        }
//
//        for (key in keys) {
//            ddb.Delete(MEM(), key, -1, false, false)
//            executor.submit(run)
//        }
//
//        executor.shutdown()
//        try {
//            executor.awaitTermination(20, TimeUnit.SECONDS)
//        } catch (e: InterruptedException) {
//            throw RuntimeException(e)
//        }
//
//        assertEquals(COUNT.toLong(), count.get().toLong())
//    }

}