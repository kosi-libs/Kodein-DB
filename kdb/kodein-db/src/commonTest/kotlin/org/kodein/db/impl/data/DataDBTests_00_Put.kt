package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.test.utils.byteArray
import kotlin.test.Test

@Suppress("ClassName")
class DataDBTests_00_Put : DataDBTests() {

    @Test
    fun test00_PutSimpleKeyWithoutIndex() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"))

        assertDBIs(
                byteArray('o', 0, 0, 0, 0, 1, "aaa", 0) to byteArray("ValueA1!")
        )
    }

    @Test
    fun test01_PutSimpleKeyWith1Index() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))

        assertDBIs(
                byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to byteArray('o', 0, 0, 0, 0, 1, "aaa", 0),
                byteArray('o', 0, 0, 0, 0, 1, "aaa", 0) to byteArray("ValueA1!"),
                byteArray('r', 0, 0, 0, 0, 1, "aaa", 0) to byteArray(0, 0, 0, 29, 'i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
        )
    }

    @Test
    fun test02_PutSimpleKeyWith2Index() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta"), "Numbers" to Value.ofAscii("forty", "two")))

        assertDBIs(
                byteArray('i', 0, 0, 0, 0, 1, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0) to byteArray('o', 0, 0, 0, 0, 1, "aaa", 0),
                byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to byteArray('o', 0, 0, 0, 0, 1, "aaa", 0),
                byteArray('o', 0, 0, 0, 0, 1, "aaa", 0) to byteArray("ValueA1!"),
                byteArray('r', 0, 0, 0, 0, 1, "aaa", 0) to byteArray(0, 0, 0, 29, 'i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0, 0, 0, 0, 28, 'i', 0, 0, 0, 0, 1, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0)
        )
    }

    @Test
    fun test03_PutTwiceWithRemovedIndex() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa", "bbb")), Value.ofAscii("ValueAB1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa", "bbb")), Value.ofAscii("ValueAB2!"))

        assertDBIs(
                byteArray('o', 0, 0, 0, 0, 1, "aaa", 0, "bbb", 0) to byteArray("ValueAB2!")
        )
    }

    @Test
    fun test04_PutTwiceWithDifferentIndex() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa", "bbb")), Value.ofAscii("ValueAB1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa", "bbb")), Value.ofAscii("ValueAB2!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        assertDBIs(
                byteArray('i', 0, 0, 0, 0, 1, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0, "bbb", 0) to byteArray('o', 0, 0, 0, 0, 1, "aaa", 0, "bbb", 0),
                byteArray('o', 0, 0, 0, 0, 1, "aaa", 0, "bbb", 0) to byteArray("ValueAB2!"),
                byteArray('r', 0, 0, 0, 0, 1, "aaa", 0, "bbb", 0) to byteArray(0, 0, 0, 32, 'i', 0, 0, 0, 0, 1, "Numbers", 0, "forty", 0, "two", 0, "aaa", 0, "bbb", 0)
        )
    }

}
