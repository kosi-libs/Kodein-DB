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
class DataDBTests_07_FindByIndex : DataDBTests() {

    @Test
    fun test00_FindByIndexAll() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC2!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "ccc", 0), byteArray("ValueC2!"), it)
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindByIndexReverse() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC2!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "ccc", 0), byteArray("ValueC2!"), it)
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test02_FindByIndexNothingInEmptyDB() {
        ddb.findAllByIndex(1, "Symbols").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test03_FindByIndexNothingInEmptyCollection() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex(2, "Symbols").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test04_FindByIndexNothingInEmptyIndex() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        ddb.findAllByIndex(1, "Names").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test05_FindByIndexComposite() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC2!"), indexSet("Symbols" to Value.ofAscii("alpha", "gamma")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ddd")), Value.ofAscii("ValueE1!"), indexSet("Symbols" to Value.ofAscii("delta", "gamma")))

        ddb.findByIndex(1, "Symbols", Value.ofAscii("alpha")).use {
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "ccc", 0), byteArray("ValueC2!"), it)
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "gamma", 0, "ccc", 0), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test06_FindByIndexReverseComposite() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC2!"), indexSet("Symbols" to Value.ofAscii("alpha", "gamma")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ddd")), Value.ofAscii("ValueE1!"), indexSet("Symbols" to Value.ofAscii("delta", "gamma")))

        ddb.findByIndex(1, "Symbols", Value.ofAscii("alpha")).use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "ccc", 0), byteArray("ValueC2!"), it)
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "gamma", 0, "ccc", 0), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0), byteArray("ValueA1!"), it)
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test07_FindByIndexCompositeUnknown() {
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("alpha", "gamma")))
        ddb.put(ddb.newKey(1, Value.ofAscii("ddd")), Value.ofAscii("ValueE1!"), indexSet("Symbols" to Value.ofAscii("delta", "gamma")))
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.findByIndex(1, "Symbols", Value.ofAscii("gamma")).use {
            assertFalse(it.isValid())
        }

    }

    @Test
    fun test08_FindByEmptyIndex() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("index" to Value.ofAscii("value")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("index" to Value.ofAscii("")))

        ddb.findByIndex(1, "index", Value.ofAscii("")).use {
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "bbb", 0), byteArray("ValueB1!"), it)

            it.next()

            assertFalse(it.isValid())
        }

    }

}
