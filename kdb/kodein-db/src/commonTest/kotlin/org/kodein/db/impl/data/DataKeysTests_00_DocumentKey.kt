package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.native
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class DataKeysTests_00_DocumentKey {

    @Test
    fun test00_SimpleKey() {
        val size = getDocumentKeySize(Value.of("one"))
        assertEquals(size, 10)
        Allocation.native(size) {
            writeDocumentKey(1, Value.of("one"))
        } .use {
            assertBytesEquals(array('o', 0, int(1), "one", 0), it)
        }
    }

    @Test
    fun test01_SimpleKeyPrefix() {
        val size = getDocumentKeySize(Value.of("one"), isOpen = true)
        assertEquals(size, 9)
        Allocation.native(size) {
            writeDocumentKey(1, Value.of("one"), isOpen = true)
        } .use {
            assertBytesEquals(array('o', 0, int(1), "one"), it)
        }
    }

    @Test
    fun test02_CompositeKey() {
        val size = getDocumentKeySize(Value.of("one", "two"))
        assertEquals(size, 14)
        Allocation.native(size) {
            writeDocumentKey(1, Value.of("one", "two"))
        } .use {
            assertBytesEquals(array('o', 0, int(1), "one", 0, "two", 0), it)
        }
    }

    @Test
    fun test03_CompositeKeyPrefix() {
        val size = getDocumentKeySize(Value.of("one", "two"), isOpen = true)
        assertEquals(size, 13)
        Allocation.native(size) {
            writeDocumentKey(1, Value.of("one", "two"), isOpen = true)
        } .use {
            assertBytesEquals(array('o', 0, int(1), "one", 0, "two"), it)
        }
    }

    @Test
    fun test04_NullKey() {
        val size = getDocumentKeySize(null)
        assertEquals(size, 6)
        Allocation.native(size) {
            writeDocumentKey(1, null)
        } .use {
            assertBytesEquals(array('o', 0, int(1)), it)
        }
    }

    @Test
    fun test05_KeyType() {
        Allocation.native(32) {
            writeDocumentKey(1, Value.of("one", "two"))
        } .use {
            val type = getDocumentKeyType(it)
            assertEquals(1, type)
        }
    }

    @Test
    fun test06_KeyID() {
        Allocation.native(32) {
            writeDocumentKey(1, Value.of("one", "two"))
        } .use {
            val id = getDocumentKeyID(it)
            assertBytesEquals(array("one", 0, "two"), id)
        }
    }
}