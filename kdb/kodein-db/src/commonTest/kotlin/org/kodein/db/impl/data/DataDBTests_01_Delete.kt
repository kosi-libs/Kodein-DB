package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.file.FileSystem
import kotlin.test.Test

@Suppress("ClassName")
abstract class DataDBTests_01_Delete : DataDBTests() {

    class LDB : DataDBTests_01_Delete() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_01_Delete() { override val factory = DataDB.inMemory }


    @Test
    fun test00_DeleteWithoutIndex() {
        val key = ddb.newKey(1, Value.ofAscii("aaa", "bbb"))
        ddb.put(key, Value.ofAscii("ValueAB1"))
        ddb.delete(key)

        assertDBIs(
        )
    }

    @Test
    fun test01_DeleteWithIndex() {
        val key = ddb.newKey(1, Value.ofAscii("aaa"))
        ddb.put(key, Value.ofAscii("ValueA1!"), mapOf("Symbols" to Value.ofAscii("alpha", "beta"), "Numbers" to Value.ofAscii("forty", "two")))
        ddb.delete(key)

        assertDBIs(
        )
    }

    @Test
    fun test02_DeleteUnknown() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), mapOf("Symbols" to Value.ofAscii("alpha", "beta")))
        val key = ddb.newKey(1, Value.ofAscii("bbb"))
        ddb.delete(key)

        assertDBIs(
                byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to byteArray('o', 0, 0, 0, 0, 1, "aaa", 0),
                byteArray('o', 0, 0, 0, 0, 1, "aaa", 0) to byteArray("ValueA1!"),
                byteArray('r', 0, 0, 0, 0, 1, "aaa", 0) to byteArray(0, 0, 0, 29, 'i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
        )
    }

    @Test
    fun test03_Delete1of2() {
        val key = ddb.newKey(1, Value.ofAscii("aaa"))
        ddb.put(key, Value.ofAscii("ValueA1!"), mapOf("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), mapOf("Numbers" to Value.ofAscii("forty", "two")))
        ddb.delete(key)

        assertDBIs(
                byteArray('i', 0, 0, 0, 0, 1, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0) to byteArray('o', 0, 0, 0, 0, 1, "bbb", 0),
                byteArray('o', 0, 0, 0, 0, 1, "bbb", 0) to byteArray("ValueB1!"),
                byteArray('r', 0, 0, 0, 0, 1, "bbb", 0) to byteArray(0, 0, 0, 28, 'i', 0, 0, 0, 0, 1, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0)
        )
    }
}
