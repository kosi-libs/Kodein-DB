package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.native
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class DataKeysTests_01_KeyInfos {

    @Test
    fun test00_KeyType() {
        Allocation.native(32).use {
            it.putDocumentKey(1, Value.ofAscii("one", "two"))
            it.flip()
            val type = getDocumentKeyType(it)
            assertEquals(1, type)
        }
    }

    @Test
    fun test01_KeyID() {
        Allocation.native(32).use {
            it.putDocumentKey(1, Value.ofAscii("one", "two"))
            it.flip()
            val id = getDocumentKeyID(it)
            assertBytesEquals(byteArray("one", 0, "two"), id)
        }
    }

}