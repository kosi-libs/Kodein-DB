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
        val aKey = ddb.newKey(1, Value.ofAscii("aaa"))
        ddb.put(aKey, Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))
        val bKey = ddb.newKey(1, Value.ofAscii("bbb"))
        ddb.put(bKey, Value.ofAscii("ValueB1!"), indexSet("Numbers" to Value.ofAscii("forty", "two")))

        assertDataIs(byteArray("ValueA1!"), ddb.get(aKey))
        assertDataIs(byteArray("ValueB1!"), ddb.get(bKey))
    }

    @Test
    fun test01_GetUnknownInEmptyDB() {
        val key = ddb.newKey(1, Value.ofAscii("aaa"))
        assertNull(ddb.get(key))
    }

    @Test
    fun test02_GetUnknownInNonEmptyDB() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), indexSet("Symbols" to Value.ofAscii("alpha", "beta")))

        assertNull(ddb.get(ddb.newKey(1, Value.ofAscii("bbb"))))
    }

}
