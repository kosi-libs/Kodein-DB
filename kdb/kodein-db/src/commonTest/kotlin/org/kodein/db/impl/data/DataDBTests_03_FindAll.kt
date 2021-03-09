package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.byteArray
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
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to Value.of("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to Value.of("forty", "two")))
        ddb.put(ddb.newKey(2, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to Value.of("gamma", "delta")))

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
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to Value.of("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to Value.of("forty", "two")))
        ddb.put(ddb.newKey(2, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to Value.of("gamma", "delta")))

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
