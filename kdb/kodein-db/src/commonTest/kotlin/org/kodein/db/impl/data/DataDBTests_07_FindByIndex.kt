package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.db.test.utils.ushort
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.asMemory
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
abstract class DataDBTests_07_FindByIndex : DataDBTests() {

    class LDB : DataDBTests_07_FindByIndex() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_07_FindByIndex() { override val factory = DataDB.inMemory }


    @Test
    fun test00_FindByIndexAll() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC1!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsC1"))))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC2!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to null)))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            assertNotNull(it.transientAssociatedData())
            assertBytesEquals(array("MetaSymbolsA"), it.transientAssociatedData()!!)
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "ccc", 0), array("ValueC2!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), it.transientSeekKey())
            assertNull(it.transientAssociatedData())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindByIndexReverse() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC1!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsC1"))))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC2!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to null)))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "ccc", 0), array("ValueC2!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0), it.transientSeekKey())
            assertNull(it.transientAssociatedData())
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            assertNotNull(it.transientAssociatedData())
            assertBytesEquals(array("MetaSymbolsA"), it.transientAssociatedData()!!)
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
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsC"))))

        ddb.findAllByIndex(2, "Symbols").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test04_FindByIndexNothingInEmptyIndex() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsC"))))

        ddb.findAllByIndex(1, "Names").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test05_FindByIndexComposite() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC1!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsC1"))))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC2!"), mapOf("Symbols" to listOf(Value.of("alpha", "gamma") to null)))
        ddb.put(ddb.newKey(1, Value.of("ddd")), Value.of("ValueE!"), mapOf("Symbols" to listOf(Value.of("delta", "gamma") to null)))

        ddb.findByIndex(1, "Symbols", Value.of("alpha")).use {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            assertNotNull(it.transientAssociatedData())
            assertBytesEquals(array("MetaSymbolsA"), it.transientAssociatedData()!!)
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "ccc", 0), array("ValueC2!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "gamma", 0, "ccc", 0), it.transientSeekKey())
            assertNull(it.transientAssociatedData())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test06_FindByIndexReverseComposite() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC1!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsC1"))))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC2!"), mapOf("Symbols" to listOf(Value.of("alpha", "gamma") to null)))
        ddb.put(ddb.newKey(1, Value.of("ddd")), Value.of("ValueE!"), mapOf("Symbols" to listOf(Value.of("delta", "gamma") to null)))

        ddb.findByIndex(1, "Symbols", Value.of("alpha")).use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "ccc", 0), array("ValueC2!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "gamma", 0, "ccc", 0), it.transientSeekKey())
            assertNull(it.transientAssociatedData())
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            assertNotNull(it.transientAssociatedData())
            assertBytesEquals(array("MetaSymbolsA"), it.transientAssociatedData()!!)
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test07_FindByIndexCompositeUnknown() {
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to listOf(Value.of("alpha", "gamma") to Value.of("MetaSymbolsC"))))
        ddb.put(ddb.newKey(1, Value.of("ddd")), Value.of("ValueE!"), mapOf("Symbols" to listOf(Value.of("delta", "gamma") to null)))
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.findByIndex(1, "Symbols", Value.of("gamma")).use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test08_FindByEmptyIndex() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("index" to listOf(Value.of("value") to null)))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("index" to listOf(Value.of("") to null)))

        ddb.findByIndex(1, "index", Value.of("")).use {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "bbb", 0), array("ValueB!"), it)

            it.next()

            assertFalse(it.isValid())
        }
    }

    @Test
    fun test09_FindByIndexMultiple() {
        ddb.put(
            ddb.newKey(1, Value.of("aaa")),
            Value.of("ValueA!"),
            mapOf(
                "Symbols" to listOf(
                    Value.of("alpha", "beta") to Value.of("MetaSymbols1"),
                    Value.of("delta", "gamma") to Value.of("MetaSymbols2")
                )
            )
        )

        ddb.findByIndex(1, "Symbols", Value.of("alpha")).use {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            assertNotNull(it.transientAssociatedData())
            assertBytesEquals(array("MetaSymbols1"), it.transientAssociatedData()!!)
            it.next()
            assertFalse(it.isValid())
        }

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            assertNotNull(it.transientAssociatedData())
            assertBytesEquals(array("MetaSymbols1"), it.transientAssociatedData()!!)
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "aaa", 0), it.transientSeekKey())
            assertNotNull(it.transientAssociatedData())
            assertBytesEquals(array("MetaSymbols2"), it.transientAssociatedData()!!)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test10_FindByIndexWithV0() {
        ddb.kv.put(array('o', 0, int(1), "aaa", 0).asMemory(), array("ValueA!").asMemory())
        ddb.kv.put(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0).asMemory(), array(128, ushort(10), ushort(3), "MetaSymbolsA").asMemory())
        ddb.kv.put(array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0).asMemory(), array(128, ushort(9), ushort(3)).asMemory())
        ddb.kv.put(array('r', 0, int(1), "aaa", 0).asMemory(), array(int(29), 'i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0, int(28), 'i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0).asMemory())
        ddb.kv.put(array('o', 0, int(1), "bbb", 0).asMemory(), array("ValueB!").asMemory())
        ddb.kv.put(array('i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0).asMemory(), array('o', 0, int(1), "bbb", 0).asMemory())
        ddb.kv.put(array('r', 0, int(1), "bbb", 0).asMemory(), array(int(30), 'i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0).asMemory())

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0), it.transientSeekKey())
            assertNotNull(it.transientAssociatedData())
            assertBytesEquals(array("MetaSymbolsA"), it.transientAssociatedData()!!)
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "bbb", 0), array("ValueB!"), it)
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0), it.transientSeekKey())
            assertNull(it.transientAssociatedData())
            it.next()
            assertFalse(it.isValid())
        }
    }
}
