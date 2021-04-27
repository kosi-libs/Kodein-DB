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
abstract class DataDBTests_06_Seek : DataDBTests() {

    class LDB : DataDBTests_06_Seek() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_06_Seek() { override val factory = DataDB.inMemory }


    @Test
    fun test00_SeekPK() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        ddb.findAllByType(1).use {
            assertTrue(it.isValid())
            val key = ddb.newKey(1, Value.of("bba"))
            it.seekTo(key)
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "bbb", 0), array("ValueB!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_SeekPKBefore() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        ddb.findAllByType(1).use {
            assertTrue(it.isValid())
            val key = ddb.newKey(1, Value.of("A"))
            it.seekTo(key)
            assertTrue(it.isValid())
            assertCursorIs(array('o', 0, int(1), "aaa", 0), array("ValueA!"), it)
        }
    }

    @Test
    fun test02_SeekPKAfter() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        ddb.findAllByType(1).use {
            assertTrue(it.isValid())
            val key = ddb.newKey(1, Value.of("z"))
            it.seekTo(key)
            assertFalse(it.isValid())
        }
    }
}
