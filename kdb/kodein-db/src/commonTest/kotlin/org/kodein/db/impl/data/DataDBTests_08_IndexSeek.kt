package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.io.wrap
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class DataDBTests_08_IndexSeek : DataDBTests() {

    class LDB : DataDBTests_08_IndexSeek() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_08_IndexSeek() { override val factory = DataDB.inMemory }


    @Test
    fun test00_SeekIndex() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsC"))))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            it.seekTo(Memory.wrap(array('i', 0, int(1), "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0)))
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "ccc", 0), array("ValueC!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_SeekIndexBefore() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsC"))))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            it.seekTo(Memory.wrap(array('i', 0, int(1), "Symbols", 0, "A", 0, "A", 0)))
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
        }
    }

    @Test
    fun test02_SeekIndexAfter() {
        ddb.put(ddb.newKey(1, Value.of("ValueA!")), Value.of("aaa"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("ValueB!")), Value.of("bbb"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ValueC!")), Value.of("ccc"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsC"))))

        ddb.findAllByIndex(1, "Symbols").use {
            assertTrue(it.isValid())
            it.seekTo(Memory.wrap(array('i', 0, int(1), "Symbols", 0, "z", 0, "z", 0)))
            assertFalse(it.isValid())
        }
    }


}
