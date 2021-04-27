package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.db.test.utils.ushort
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import kotlin.test.Test

@Suppress("ClassName")
abstract class DataDBTests_09_Batch : DataDBTests() {

    class LDB : DataDBTests_09_Batch() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_09_Batch() { override val factory = DataDB.inMemory }


    @Test
    fun test00_BatchPut() {

        val batch = ddb.newBatch()
        batch.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        batch.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        assertDBIs()

        MaybeThrowable().also { batch.write(it) } .shoot()

        assertDBIs(
                array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "bbb", 0) to array(128, ushort(9), ushort(3)),
                array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to array(128, ushort(10), ushort(3), "MetaSymbolsA"),
                array('o', 0, int(1), "aaa", 0) to array("ValueA!"),
                array('o', 0, int(1), "bbb", 0) to array("ValueB!"),
                array('r', 0, int(1), "aaa", 0) to array(128, "Symbols", 0, int(12), ushort(10), "alpha", 0, "beta"),
                array('r', 0, int(1), "bbb", 0) to array(128, "Numbers", 0, int(11), ushort(9), "forty", 0, "two")
        )
    }

    @Test
    fun test01_BatchDelete() {

        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        ddb.put(ddb.newKey(1, Value.of("bbb")), Value.of("ValueB!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))
        ddb.put(ddb.newKey(1, Value.of("ccc")), Value.of("ValueC!"), mapOf("Symbols" to listOf(Value.of("gamma", "delta") to Value.of("MetaSymbolsC"))))

        ddb.newBatch().use { batch ->
            batch.delete(ddb.newKey(1, Value.of("aaa")))
            batch.delete(ddb.newKey(1, Value.of("bbb")))

            assertDBIs(
                    array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "bbb", 0) to array(128, ushort(9), ushort(3)),
                    array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to array(128, ushort(10), ushort(3), "MetaSymbolsA"),
                    array('i', 0, int(1), "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0) to array(128, ushort(11), ushort(3), "MetaSymbolsC"),
                    array('o', 0, int(1), "aaa", 0) to array("ValueA!"),
                    array('o', 0, int(1), "bbb", 0) to array("ValueB!"),
                    array('o', 0, int(1), "ccc", 0) to array("ValueC!"),
                    array('r', 0, int(1), "aaa", 0) to array(128, "Symbols", 0, int(12), ushort(10), "alpha", 0, "beta"),
                    array('r', 0, int(1), "bbb", 0) to array(128, "Numbers", 0, int(11), ushort(9), "forty", 0, "two"),
                    array('r', 0, int(1), "ccc", 0) to array(128, "Symbols", 0, int(13), ushort(11), "gamma", 0, "delta")
            )

            MaybeThrowable().also { batch.write(it) } .shoot()

            assertDBIs(
                    array('i', 0, int(1), "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0) to array(128, ushort(11), ushort(3), "MetaSymbolsC"),
                    array('o', 0, int(1), "ccc", 0) to array("ValueC!"),
                    array('r', 0, int(1), "ccc", 0) to array(128, "Symbols", 0, int(13), ushort(11), "gamma", 0, "delta")
            )
        }
    }

    @Test
    fun test02_BatchOverride() {

        val batch = ddb.newBatch()
        batch.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueBatch!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbols"))))

        assertDBIs()

        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValuePut!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        assertDBIs(
                array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0) to array(128, ushort(9), ushort(3)),
                array('o', 0, int(1), "aaa", 0) to array("ValuePut!"),
                array('r', 0, int(1), "aaa", 0) to array(128, "Numbers", 0, int(11), ushort(9), "forty", 0, "two")
        )

        MaybeThrowable().also { batch.write(it) } .shoot()

        assertDBIs(
                array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to array(128, ushort(10), ushort(3), "MetaSymbols"),
                array('o', 0, int(1), "aaa", 0) to array("ValueBatch!"),
                array('r', 0, int(1), "aaa", 0) to array(128, "Symbols", 0, int(12), ushort(10), "alpha", 0, "beta")
        )
    }
}
