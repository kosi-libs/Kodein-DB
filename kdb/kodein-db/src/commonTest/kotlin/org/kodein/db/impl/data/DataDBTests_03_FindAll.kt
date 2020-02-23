package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class DataDBTests_03_FindAll : DataDBTests() {

    @Test
    fun test00_FindAll() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(2, Value.ofAscii("ccc")), Value.ofAscii("ValueC!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        ddb.findAll().use {
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0), byteArray("ValueA!"), it)
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "bbb", 0), byteArray("ValueB!"), it)
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 2, "ccc", 0), byteArray("ValueC!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindAllReverse() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(2, Value.ofAscii("ccc")), Value.ofAscii("ValueC!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        ddb.findAll().use {
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 2, "ccc", 0), byteArray("ValueC!"), it)
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "bbb", 0), byteArray("ValueB!"), it)
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0), byteArray("ValueA!"), it)
            it.prev()
            assertFalse(it.isValid())
        }
    }

}
