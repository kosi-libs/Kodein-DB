package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.native
import org.kodein.memory.text.Charset
import org.kodein.memory.text.wrap
import org.kodein.memory.use
import kotlin.test.Test

@Suppress("ClassName")
class DataKeysTests_01_KeyInfos {

    @Test
    fun test00_KeyType() {
        Allocation.native(32).use {
            it.putObjectKey(KBuffer.wrap("Test", Charset.ASCII), Value.ofAscii("one", "two"))
            it.flip()
            val type = getObjectKeyType(it)
            assertBytesEquals(byteArray("Test"), type)
        }
    }

    @Test
    fun test01_KeyID() {
        Allocation.native(32).use {
            it.putObjectKey(KBuffer.wrap("Test", Charset.ASCII), Value.ofAscii("one", "two"))
            it.flip()
            val id = getObjectKeyID(it)
            assertBytesEquals(byteArray("one", 0, "two"), id)
        }
    }

}