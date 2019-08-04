package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class DataDBTests_05_FindByPK : DataDBTests() {

    @Test
    fun test00_FindByPKCompositeKey() {
        ddb.put("Test", Value.ofAscii("aaa", "a"), Value.ofAscii("ValueAa1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("aaa", "b"), Value.ofAscii("ValueAb1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findByPrimaryKey("Test", Value.ofAscii("aaa")).use {
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "aaa", 0, 'a', 0), byteArray("ValueAa1!"), it)
            assertBytesEquals(it.transientKey().bytes, it.transientSeekKey().bytes)
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "aaa", 0, 'b', 0), byteArray("ValueAb1!"), it)
            assertBytesEquals(it.transientKey().bytes, it.transientSeekKey().bytes)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindByPKReverseCompositeKey() {
        ddb.put("Test", Value.ofAscii("aaa", "a"), Value.ofAscii("ValueAa1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("aaa", "b"), Value.ofAscii("ValueAb1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findByPrimaryKey("Test", Value.ofAscii("aaa")).use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "aaa", 0, 'b', 0), byteArray("ValueAb1!"), it)
            assertBytesEquals(it.transientKey().bytes, it.transientSeekKey().bytes)
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "aaa", 0, 'a', 0), byteArray("ValueAa1!"), it)
            assertBytesEquals(it.transientKey().bytes, it.transientSeekKey().bytes)
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test02_FindByPKUnknownKey() {
        ddb.put( "Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put( "Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findByPrimaryKey("Test", Value.ofAscii("ccc")).use {
            assertFalse(it.isValid())
        }
    }
}
