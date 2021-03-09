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
abstract class DataDBTests_06_Seek : DataDBTests() {

    class LDB : DataDBTests_06_Seek() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_06_Seek() { override val factory = DataDB.inMemory }


    @Test
    fun test00_SeekPK() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA1!"), mapOf("Symbols" to Value.of("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB1!"), mapOf("Numbers" to Value.of("forty", "two")))

        ddb.findAllByType(1).use {
            assertTrue(it.isValid())
            val key = ddb.newKey(1, Value.of("bba"))
            it.seekTo(key)
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "bbb", 0), byteArray("ValueB1!"), it)
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_SeekPKBefore() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA1!"), mapOf("Symbols" to Value.of("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB1!"), mapOf("Numbers" to Value.of("forty", "two")))

        ddb.findAllByType(1).use {
            assertTrue(it.isValid())
            val key = ddb.newKey(1, Value.of("A"))
            it.seekTo(key)
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, 0, 0, 0, 1, "aaa", 0), byteArray("ValueA1!"), it)
        }
    }

    @Test
    fun test02_SeekPKAfter() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA1!"), mapOf("Symbols" to Value.of("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB1!"), mapOf("Numbers" to Value.of("forty", "two")))

        ddb.findAllByType(1).use {
            assertTrue(it.isValid())
            val key = ddb.newKey(1, Value.of("z"))
            it.seekTo(key)
            assertFalse(it.isValid())
        }
    }
}
