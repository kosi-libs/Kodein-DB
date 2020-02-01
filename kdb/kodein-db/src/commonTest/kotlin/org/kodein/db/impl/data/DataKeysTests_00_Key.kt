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
import kotlin.test.assertEquals

@Suppress("ClassName")
class DataKeysTests_00_Key {
    @Test
    fun test00_SimpleKey() {
        val size = getObjectKeySize(4, Value.ofAscii("one"))
        assertEquals(size, 11)
        Allocation.native(size).use {
            it.putObjectKey(KBuffer.wrap("Test", Charset.ASCII), Value.ofAscii("one"))
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0), it)
        }
    }

    @Test
    fun test01_SimpleKeyPrefix() {
        val size = getObjectKeySize(4, Value.ofAscii("one"), isOpen = true)
        assertEquals(size, 10)
        Allocation.native(size).use {
            it.putObjectKey(KBuffer.wrap("Test", Charset.ASCII), Value.ofAscii("one"), isOpen = true)
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0, "one"), it)
        }
    }

    @Test
    fun test02_CompositeKey() {
        val size = getObjectKeySize(4, Value.ofAscii("one", "two"))
        assertEquals(size, 15)
        Allocation.native(size).use {
            it.putObjectKey(KBuffer.wrap("Test", Charset.ASCII), Value.ofAscii("one", "two"))
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0, "two", 0), it)
        }
    }

    @Test
    fun test03_CompositeKeyPrefix() {
        val size = getObjectKeySize(4, Value.ofAscii("one", "two"), isOpen = true)
        assertEquals(size, 14)
        Allocation.native(size).use {
            it.putObjectKey(KBuffer.wrap("Test", Charset.ASCII), Value.ofAscii("one", "two"), isOpen = true)
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0, "two"), it)
        }
    }

    @Test
    fun test04_NullKey() {
        val size = getObjectKeySize(4, null)
        assertEquals(size, 7)
        Allocation.native(size).use {
            it.putObjectKey(KBuffer.wrap("Test", Charset.ASCII), null)
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0), it)
        }
    }
}