package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class DataDBTests_03_FindAll : DataDBTests() {

    class LDB : DataDBTests_03_FindAll() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_03_FindAll() { override val factory = DataDB.inMemory }


    @Test
    fun test00_FindAll() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(2, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to null)))

        ddb.findAll().use {
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "bbb", 0), array("ValueB!"), it)
            it.next()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(2), "ccc", 0), array("ValueC!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindAllReverse() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(2, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to null)))

        ddb.findAll().use {
            it.seekToLast()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(2), "ccc", 0), array("ValueC!"), it)
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "bbb", 0), array("ValueB!"), it)
            it.prev()
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
            it.prev()
            assertFalse(it.isValid())
        }
    }

}
