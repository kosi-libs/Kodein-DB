package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.db.test.utils.ushort
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.asMemory
import kotlin.test.Test

@Suppress("ClassName")
abstract class DataDBTests_01_Delete : DataDBTests() {

    class LDB : DataDBTests_01_Delete() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_01_Delete() { override val factory = DataDB.inMemory }


    @Test
    fun test00_DeleteWithoutIndex() {
        val key = ddb.newKey(1, Value.of("aaa", "bbb"))
        ddb.put(key, Value.of("ValueAB"))
        ddb.delete(key)

        assertDBIs(
        )
    }

    @Test
    fun test01_DeleteWithIndex() {
        val key = ddb.newKey(1, Value.of("aaa"))
        ddb.put(key, Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to null), "Numbers" to listOf(Value.of("forty", "two") to Value.of("MetaNumbersA"))))
        ddb.delete(key)

        assertDBIs(
        )
    }

    @Test
    fun test02_DeleteUnknown() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        val key = ddb.newKey(1, Value.of("bbb"))
        ddb.delete(key)

        assertDBIs(
                array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to array(128, ushort(10), ushort(3), "MetaSymbolsA"),
                array('o', 0, int(1), "aaa", 0) to array("ValueA!"),
                array('r', 0, int(1), "aaa", 0) to array(128, "Symbols", 0, int(12), ushort(10), "alpha", 0, "beta")
        )
    }

    @Test
    fun test03_Delete1of2() {
        val key = ddb.newKey(1, Value.of("aaa"))
        ddb.put(key, Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.delete(key)

        assertDBIs(
                array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "bbb", 0) to array(128, ushort(9), ushort(3)),
                array('o', 0, int(1), "bbb", 0) to array("ValueB!"),
                array('r', 0, int(1), "bbb", 0) to array(128, "Numbers", 0, int(11), ushort(9), "forty", 0, "two")
        )
    }

    @Test
    fun test04_DeleteV0WithIndex() {
        ddb.kv.put(array('o', 0, int(1), "aaa", 0).asMemory(), array("ValueA!").asMemory())
        ddb.kv.put(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0).asMemory(), array('o', 0, int(1), "aaa", 0).asMemory())
        ddb.kv.put(array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0).asMemory(), array('o', 0, int(1), "aaa", 0).asMemory())
        ddb.kv.put(array('r', 0, int(1), "aaa", 0).asMemory(), array(int(29), 'i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0, int(28), 'i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0).asMemory())
        ddb.kv.put(array('o', 0, int(1), "bbb", 0).asMemory(), array("ValueB!").asMemory())
        ddb.kv.put(array('i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0).asMemory(), array('o', 0, int(1), "bbb", 0).asMemory())
        ddb.kv.put(array('r', 0, int(1), "bbb", 0).asMemory(), array(int(30), 'i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0).asMemory())

        ddb.delete(ddb.newKey(1, Value.of("aaa")))

        assertDBIs(
            array('i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0) to array('o', 0, int(1), "bbb", 0),
            array('o', 0, int(1), "bbb", 0) to array("ValueB!"),
            array('r', 0, int(1), "bbb", 0) to array( int(30), 'i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0)
        )
    }
}
