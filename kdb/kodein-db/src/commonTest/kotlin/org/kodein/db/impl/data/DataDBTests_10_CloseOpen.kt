package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class DataDBTests_10_CloseOpen : DataDBTests() {

    @Test
    fun test090_PutCloseOpenGet() {
        ddb.put("Test", Value.ofAscii("key"), Value.ofAscii("value"))

        ddb.close()

        _ddb = DataDBTestFactory.open()

        val key = ddb.getKey("Test", Value.ofAscii("key"))
        assertBytesEquals(byteArray("value"), ddb.get(key)!!)
    }

    @Test
    fun test091_PutCloseOpenIter() {
        ddb.put("Test", Value.ofAscii("key"), Value.ofAscii("value"))

        ddb.close()

        _ddb = DataDBTestFactory.open()

        val it = ddb.findByType("Test")
        try {
            assertTrue(it.isValid())
            assertCursorIs(byteArray('o', 0, "Test", 0, "key", 0), byteArray("value"), it)

            it.next()
            assertFalse(it.isValid())
        } finally {
            it.close()
        }
    }

}
