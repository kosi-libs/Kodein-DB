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
abstract class DataDBTests_04_FindByType : DataDBTests() {

    class LDB : DataDBTests_04_FindByType() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_04_FindByType() { override val factory = DataDB.inMemory }


    @Test
    fun test00_FindByTypeAll() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB1!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsB"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB2!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        ddb.findAllByType(1).use {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "bbb", 0), array("ValueB2!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindByTypeAllReverse() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB1!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsB"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB2!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        ddb.findAllByType(1).use {
            assertTrue(it.isValid())
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "bbb", 0), array("ValueB2!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            assertBytesEquals(it.transientKey(), it.transientSeekKey())
            it.prev()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test02_FindByTypeNothingInEmptyDB() {
        ddb.findAllByType(1).use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test03_FindByTypeNothingInEmptyCollection() {
        ddb.put(ddb.newKey(1, Value.of("ValueA!")), Value.of("aaa"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("ValueB!")), Value.of("bbb"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        ddb.findAllByType(2).use {
            assertFalse(it.isValid())
        }
    }

}
