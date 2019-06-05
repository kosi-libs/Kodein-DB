package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.test.utils.byteArray
import kotlin.test.Test

@Suppress("ClassName")
class DataDBTests_01_Delete : DataDBTests() {

    @Test
    fun test00_DeleteWithoutIndex() {
        val key = ddb.putAndGetHeapKey("Test", Value.ofAscii("aaa", "bbb"), Value.ofAscii("ValueAB1")).value
        ddb.delete(key)

        assertDBIs(
        )
    }

    @Test
    fun test01_DeleteWithIndex() {
        val key = ddb.putAndGetHeapKey("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta"), "Numbers" to Value.ofAscii("forty", "two"))).value
        ddb.delete(key)

        assertDBIs(
        )
    }

    @Test
    fun test02_DeleteUnknown() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        val key = ddb.getHeapKey("Test", Value.ofAscii("bbb"))
        ddb.delete(key)

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0) to byteArray('o', 0, "Test", 0, "aaa", 0),
                byteArray('o', 0, "Test", 0, "aaa", 0) to byteArray("ValueA1!"),
                byteArray('r', 0, "Test", 0, "aaa", 0) to byteArray(0, 0, 0, 30, 'i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0)
        )
    }

    @Test
    fun test03_Delete1of2() {
        val key = ddb.putAndGetHeapKey("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta"))).value
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))
        ddb.delete(key)

        assertDBIs(
                byteArray('i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0) to byteArray('o', 0, "Test", 0, "bbb", 0),
                byteArray('o', 0, "Test", 0, "bbb", 0) to byteArray("ValueB1!"),
                byteArray('r', 0, "Test", 0, "bbb", 0) to byteArray(0, 0, 0, 29, 'i', 0, "Test", 0, "Numbers", 0, "forty", 0, "two", 0, "bbb", 0)
        )
    }
}
