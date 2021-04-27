package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class DataDBTests_05_FindByID : DataDBTests() {

    class LDB : DataDBTests_05_FindByID() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_05_FindByID() { override val factory = DataDB.inMemory }


    @Test
    fun test00_FindByPKCompositeKey() {
        ddb.put(ddb.newKey(1, Value.of("aaa", "a")), Value.of("ValueAa!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsAa"))))
        ddb.put(ddb.newKey(1, Value.of("aaa", "b")), Value.of("ValueAb!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsAb"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        ddb.findById(1, Value.of("aaa")).use {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0, 'a', 0), array("ValueAa!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0, 'b', 0), array("ValueAb!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindByPKReverseCompositeKey() {
        ddb.put(ddb.newKey(1, Value.of("aaa", "a")), Value.of("ValueAa!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsAa"))))
        ddb.put(ddb.newKey(1, Value.of("aaa", "b")), Value.of("ValueAb!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsAb"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        ddb.findById(1, Value.of("aaa")).use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0, 'b', 0), array("ValueAb!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0, 'a', 0), array("ValueAa!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test02_FindByPKUnknownKey() {
        ddb.put( ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put( ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        ddb.findById(1, Value.of("ccc")).use {
            assertFalse(it.isValid())
        }
    }
}
