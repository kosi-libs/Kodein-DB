package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class DataDBTests_06_Seek : DataDBTests() {

    @Test
    fun test00_SeekPK() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            val key = ddb.getKey("Test", Value.ofAscii("bba"))
            it.seekTo(key)
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "bbb", 0), byteArray("ValueB1!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_SeekPKBefore() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            val key = ddb.getKey("Test", Value.ofAscii("A"))
            it.seekTo(key)
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "aaa", 0), byteArray("ValueA1!"), it)
        }
    }

    @Test
    fun test02_SeekPKAfter() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        ddb.findAllByType("Test").use {
            assertTrue(it.isValid())
            val key = ddb.getKey("Test", Value.ofAscii("z"))
            it.seekTo(key)
            assertFalse(it.isValid())
        }
    }
}
