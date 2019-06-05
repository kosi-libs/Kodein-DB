package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.test.utils.byteArray
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("ClassName")
class DataDBTests_02_Get : DataDBTests() {

    @Test
    fun test00_GetExisting() {
        val aKey = ddb.putAndGetHeapKey("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta"))).value
        ddb.put("Test", Value.ofAscii("bbb"), Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        assertDataIs(byteArray("ValueA1!"), ddb.get(aKey))

        val bKey = ddb.getHeapKey("Test", Value.ofAscii("bbb"))
        assertDataIs(byteArray("ValueB1!"), ddb.get(bKey))
    }

    @Test
    fun test01_GetUnknownInEmptyDB() {
        val key = ddb.getHeapKey("Test", Value.ofAscii("aaa"))
        assertNull(ddb.get(key))
    }

    @Test
    fun test02_GetUnknownInNonEmptyDB() {
        ddb.put("Test", Value.ofAscii("aaa"), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))

        val key = ddb.getHeapKey("Test", Value.ofAscii("bbb"))
        assertNull(ddb.get(key))
    }

}
