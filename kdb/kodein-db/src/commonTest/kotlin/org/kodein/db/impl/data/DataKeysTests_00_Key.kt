package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.Allocation
import org.kodein.memory.native
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class DataKeysTests_00_Key {
    @Test
    fun test00_SimpleKey() {
        val size = getObjectKeySize("Test", Value.ofAscii("one"))
        assertEquals(size, 11)
        Allocation.native(size).use {
            it.putObjectKey("Test", Value.ofAscii("one"))
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0), it)
        }
    }

    @Test
    fun test01_SimpleKeyPrefix() {
        val size = getObjectKeySize("Test", Value.ofAscii("one"), isOpen = true)
        assertEquals(size, 10)
        Allocation.native(size).use {
            it.putObjectKey("Test", Value.ofAscii("one"), isOpen = true)
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0, "one"), it)
        }
    }

    @Test
    fun test02_CompositeKey() {
        val size = getObjectKeySize("Test", Value.ofAscii("one", "two"))
        assertEquals(size, 15)
        Allocation.native(size).use {
            it.putObjectKey("Test", Value.ofAscii("one", "two"))
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0, "two", 0), it)
        }
    }

    @Test
    fun test03_CompositeKeyPrefix() {
        val size = getObjectKeySize("Test", Value.ofAscii("one", "two"), isOpen = true)
        assertEquals(size, 14)
        Allocation.native(size).use {
            it.putObjectKey("Test", Value.ofAscii("one", "two"), isOpen = true)
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0, "two"), it)
        }
    }

    @Test
    fun test04_NullKey() {
        val size = getObjectKeySize("Test", null)
        assertEquals(size, 7)
        Allocation.native(size).use {
            it.putObjectKey("Test", null)
            it.flip()
            assertBytesEquals(byteArray('o', 0, "Test", 0), it)
        }
    }
}