package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.indexSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("ClassName")
class DataDBTests_11_FindIndexes : DataDBTests() {

    @Test
    fun test00_FindIndexes() {
        val key = ddb.putAndGetHeapKey("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA!"), indexSet("Numbers" to Value.ofAscii("forty", "two"), "Symbols" to Value.ofAscii("alpha", "beta"))).value
        val indexes = ddb.getIndexesOf(key)

        assertEquals(2, indexes.size.toLong())
        assertTrue(indexes.contains("Numbers"))
        assertTrue(indexes.contains("Symbols"))
    }

    @Test
    fun test01_FindNoIndexes() {
        val key = ddb.putAndGetHeapKey("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA!")).value
        val indexes = ddb.getIndexesOf(key)
        assertTrue(indexes.isEmpty())
    }

    @Test
    fun test02_FindUnknownIndexes() {
        val indexes = ddb.getIndexesOf(ddb.getHeapKey("Unknown", Value.ofAscii("A")))

        assertTrue(indexes.isEmpty())
    }

}
