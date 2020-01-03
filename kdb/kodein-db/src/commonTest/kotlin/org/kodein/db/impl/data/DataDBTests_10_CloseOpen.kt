package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
class DataDBTests_10_CloseOpen : DataDBTests() {

    @Test
    fun test090_PutCloseOpenGet() {
        ddb.put(ddb.newKey("Test", Value.ofAscii("key")), Value.ofAscii("value"))

        ddb.close()

        open()

        val key = ddb.newKey("Test", Value.ofAscii("key"))
        ddb.get(key)!!.use {
            assertBytesEquals(byteArray("value"), it)
        }
    }

    @Test
    fun test091_PutCloseOpenIter() {
        ddb.put(ddb.newKey("Test", Value.ofAscii("key")), Value.ofAscii("value"))

        ddb.close()

        open()

        val it = ddb.findAllByType("Test")
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
