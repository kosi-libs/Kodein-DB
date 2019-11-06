package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.util.MaybeThrowable
import kotlin.test.Test

@Suppress("ClassName")
class DataDBTests_09_Batch : DataDBTests() {
    
    @Test
    fun test00_BatchPut() {

        val batch = ddb.newBatch()
        batch.put(ddb.newHeapKey("Test", Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        batch.put(ddb.newHeapKey("Test", Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        assertDBIs()

        MaybeThrowable().also { batch.write(it) } .shoot()

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0) to byteArray('o', 0, "Test", 0, "bbb", 0),
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0) to byteArray("ValueA1!"),
                byteArray('o', 0, "Test", 0, "bbb", 0) to byteArray("ValueB1!"),
                byteArray('r', 0, "Test", 0, "aaa", 0) to byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0),
                byteArray('r', 0, "Test", 0, "bbb", 0) to byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0)
        )
    }

    @Test
    fun test01_BatchDelete() {

        ddb.put(ddb.newHeapKey("Test", Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newHeapKey("Test", Value.ofAscii("bbb")), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.put(ddb.newHeapKey("Test", Value.ofAscii("ccc")), Value.ofAscii("ValueC1!"), indexSet("Symbols" to Value.ofAscii("gamma", "delta")))

        val batch = ddb.newBatch()
        batch.delete(ddb.newHeapKey("Test", Value.ofAscii("aaa")))
        batch.delete(ddb.newHeapKey("Test", Value.ofAscii("bbb")))

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0) to byteArray('o', 0, "Test", 0, "bbb", 0),
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0) to byteArray('o', 0, "Test", 0, "ccc", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0) to byteArray("ValueA1!"),
                byteArray('o', 0, "Test", 0, "bbb", 0) to byteArray("ValueB1!"),
                byteArray('o', 0, "Test", 0, "ccc", 0) to byteArray("ValueC1!"),
                byteArray('r', 0, "Test", 0, "aaa", 0) to byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0),
                byteArray('r', 0, "Test", 0, "bbb", 0) to byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0),
                byteArray('r', 0, "Test", 0, "ccc", 0) to byteArray(0, 0, 0, 31, 'i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0)
        )

        MaybeThrowable().also { batch.write(it) } .shoot()

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0) to byteArray('o', 0, "Test", 0, "ccc", 0),
                byteArray('o', 0, "Test", 0, "ccc", 0) to byteArray("ValueC1!"),
                byteArray('r', 0, "Test", 0, "ccc", 0) to byteArray(0, 0, 0, 31, 'i', 0, "Test", 0, "Symbols", 0, "gamma", 0, "delta", 0, "ccc", 0)
        )
    }

    @Test
    fun test02_BatchOverride() {

        val batch = ddb.newBatch()
        batch.put(ddb.newHeapKey("Test", Value.ofAscii("aaa")), Value.ofAscii("ValueBatch!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))

        assertDBIs()

        ddb.put(ddb.newHeapKey("Test", Value.ofAscii("aaa")), Value.ofAscii("ValuePut!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0) to byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0) to byteArray("ValuePut!"),
                byteArray('r', 0, "Test", 0, "aaa", 0) to byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0)
        )

        MaybeThrowable().also { batch.write(it) } .shoot()

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0) to byteArray("ValueBatch!"),
                byteArray('r', 0, "Test", 0, "aaa", 0) to byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
        )
    }
}
