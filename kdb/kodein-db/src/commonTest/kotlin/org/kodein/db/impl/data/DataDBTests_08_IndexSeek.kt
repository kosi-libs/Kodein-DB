package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.wrap
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class DataDBTests_08_IndexSeek : DataDBTests() {

    @Test
    fun test00_SeekIndex() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            it.seekTo(KBuffer.wrap(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0)))
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "ccc", 0), byteArray("ValueC1!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_SeekIndexBefore() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            it.seekTo(KBuffer.wrap(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "A", 0, "A", 0)))
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0), byteArray("ValueA1!"), it)
        }
    }

    @Test
    fun test02_SeekIndexAfter() {
        ddb.put(ddb.newKey(1, Value.ofAscii("ValueA1!")), Value.ofAscii("aaa"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ValueB1!")), Value.ofAscii("bbb"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ValueC1!")), Value.ofAscii("ccc"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            it.seekTo(KBuffer.wrap(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "z", 0, "z", 0)))
            assertFalse(it.isValid())
        }
    }


}
