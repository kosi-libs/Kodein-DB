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
class DataKeysTests_00_Key {
    @Test
    fun test00_SimpleKey() {
        val size = getDocumentKeySize(Value.ofAscii("one"))
        assertEquals(size, 10)
        Allocation.native(size).use {
            it.putDocumentKey(1, Value.ofAscii("one"))
            it.flip()
            assertBytesEquals(byteArray('o', 0, 0, 0, 0, 1, "one", 0), it)
        }
    }

    @Test
    fun test01_SimpleKeyPrefix() {
        val size = getDocumentKeySize(Value.ofAscii("one"), isOpen = true)
        assertEquals(size, 9)
        Allocation.native(size).use {
            it.putDocumentKey(1, Value.ofAscii("one"), isOpen = true)
            it.flip()
            assertBytesEquals(byteArray('o', 0, 0, 0, 0, 1, "one"), it)
        }
    }

    @Test
    fun test02_CompositeKey() {
        val size = getDocumentKeySize(Value.ofAscii("one", "two"))
        assertEquals(size, 14)
        Allocation.native(size).use {
            it.putDocumentKey(1, Value.ofAscii("one", "two"))
            it.flip()
            assertBytesEquals(byteArray('o', 0, 0, 0, 0, 1, "one", 0, "two", 0), it)
        }
    }

    @Test
    fun test03_CompositeKeyPrefix() {
        val size = getDocumentKeySize(Value.ofAscii("one", "two"), isOpen = true)
        assertEquals(size, 13)
        Allocation.native(size).use {
            it.putDocumentKey(1, Value.ofAscii("one", "two"), isOpen = true)
            it.flip()
            assertBytesEquals(byteArray('o', 0, 0, 0, 0, 1, "one", 0, "two"), it)
        }
    }

    @Test
    fun test04_NullKey() {
        val size = getDocumentKeySize(null)
        assertEquals(size, 6)
        Allocation.native(size).use {
            it.putDocumentKey(1, null)
            it.flip()
            assertBytesEquals(byteArray('o', 0, 0, 0, 0, 1), it)
        }
    }
}