package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.db.test.utils.description
import org.kodein.memory.readBytes
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class DataDBTests_04_FindByType : DataDBTests() {

    @Test
    fun test00_FindByTypeAll() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB2!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB2!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindByTypeAllReverse() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test02_FindByTypeNothingInEmptyDB() {
        ddb.findAllByType("Test").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test03_FindByTypeNothingInEmptyCollection() {
        ddb.put("Test", Value.ofAscii("ValueA1!"), Value.ofAscii("aaa"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("ValueB1!"), Value.ofAscii("bbb"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findAllByType("Yeah").use {
            assertFalse(it.isValid())
        }
    }

}
