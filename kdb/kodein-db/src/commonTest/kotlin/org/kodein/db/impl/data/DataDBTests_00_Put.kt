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
abstract class DataDBTests_00_Put : DataDBTests() {

    class LDB : DataDBTests_00_Put() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_00_Put() { override val factory = DataDB.inMemory }


    @Test
    fun test00_PutSimpleKeyWithoutIndex() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"))

        assertDBIs(
                array('o', 0, int(1), "aaa", 0) to array("ValueA!")
        )
    }

    @Test
    fun test01_PutSimpleKeyWith1Index() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))

        assertDBIs(
                array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to array(128, ushort(10), ushort(3), "MetaSymbolsA"),
                array('o', 0, int(1), "aaa", 0) to array("ValueA!"),
                array('r', 0, int(1), "aaa", 0) to array(128, "Symbols", 0, int(12), ushort(10), "alpha", 0, "beta")
        )
    }

    @Test
    fun test02_PutSimpleKeyWith2Index() {
        ddb.put(
            ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"),
            mapOf(
                "Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA")),
                "Numbers" to listOf(Value.of("forty", "two") to null)
            )
        )

        assertDBIs(
                array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0) to array(128, ushort(9), ushort(3)),
                array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to array(128, ushort(10), ushort(3), "MetaSymbolsA"),
                array('o', 0, int(1), "aaa", 0) to array("ValueA!"),
                array('r', 0, int(1), "aaa", 0) to array(128, "Symbols", 0, int(12), ushort(10), "alpha", 0, "beta", "Numbers", 0, int(11), ushort(9), "forty", 0, "two")
        )
    }

    @Test
    fun test03_PutTwiceWithRemovedIndex() {
        ddb.put(ddb.newKey(1, Value.of("aaa", "bbb")), Value.of("ValueAB1!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsAB"))))
        ddb.put(ddb.newKey(1, Value.of("aaa", "bbb")), Value.of("ValueAB2!"))

        assertDBIs(
                array('o', 0, int(1), "aaa", 0, "bbb", 0) to array("ValueAB2!")
        )
    }

    @Test
    fun test04_PutTwiceWithDifferentIndex() {
        ddb.put(ddb.newKey(1, Value.of("aaa", "bbb")), Value.of("ValueAB1!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("aaa", "bbb")), Value.of("ValueAB2!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        assertDBIs(
                array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0, "bbb", 0) to array(128, ushort(9), ushort(7)),
                array('o', 0, int(1), "aaa", 0, "bbb", 0) to array("ValueAB2!"),
                array('r', 0, int(1), "aaa", 0, "bbb", 0) to array(128, "Numbers", 0, int(11), ushort(9), "forty", 0, "two")
        )
    }

    @Test
    fun test05_PutWithIndexMultiple() {
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

        assertDBIs(
            array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to array(128, ushort(10), ushort(3), "MetaSymbols1"),
            array('i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "aaa", 0) to array(128, ushort(11), ushort(3), "MetaSymbols2"),
            array('o', 0, int(1), "aaa", 0) to array("ValueA!"),
            array('r', 0, int(1), "aaa", 0) to array(128, "Symbols", 0, int(25), ushort(10), "alpha", 0, "beta", ushort(11), "delta", 0, "gamma")
        )

    }

    @Test
    fun test06_PutOverV0() {
        ddb.kv.put(array('o', 0, int(1), "aaa", 0).asMemory(), array("ValueA1!").asMemory())
        ddb.kv.put(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0).asMemory(), array('o', 0, int(1), "aaa", 0).asMemory())
        ddb.kv.put(array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0).asMemory(), array('o', 0, int(1), "aaa", 0).asMemory())
        ddb.kv.put(array('r', 0, int(1), "aaa", 0).asMemory(), array(int(29), 'i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0, int(28), 'i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0).asMemory())
        ddb.kv.put(array('o', 0, int(1), "bbb", 0).asMemory(), array("ValueB!").asMemory())
        ddb.kv.put(array('i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0).asMemory(), array('o', 0, int(1), "bbb", 0).asMemory())
        ddb.kv.put(array('r', 0, int(1), "bbb", 0).asMemory(), array(int(30), 'i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0).asMemory())

        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA2!"), mapOf("IndexName" to listOf(Value.of("IndexValue") to Value.of("IndexMetadata"))))

        assertDBIs(
            array('i', 0, int(1), "IndexName", 0, "IndexValue", 0, "aaa", 0) to array(128, ushort(10), ushort(3), "IndexMetadata"),
            array('i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0) to array('o', 0, int(1), "bbb", 0),
            array('o', 0, int(1), "aaa", 0) to array("ValueA2!"),
            array('o', 0, int(1), "bbb", 0) to array("ValueB!"),
            array('r', 0, int(1), "aaa", 0) to array(128, "IndexName", 0, int(12), ushort(10), "IndexValue"),
            array('r', 0, int(1), "bbb", 0) to array(int(30), 'i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "bbb", 0)
        )
    }

}
