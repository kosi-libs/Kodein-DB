package org.kodein.db.impl

import kotlinx.io.core.use
import org.kodein.db.Value
import org.kodein.db.leveldb.Buffer
import kotlin.test.*

class DataKeysTests {
    @Test
    fun test00_SimpleKey() {
        val type = "Test"
        val value = Value.ofAscii("one")

        val size = getObjectKeySize(type, value)
        Buffer.allocHeapBuffer(size).use {
            it.io.writeObjectKey(type, value)
            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0), it)
        }
    }

    @Test
    fun test01_SimpleKeyPrefix() {
        val objectKey = DataKeys.getObjectKey("Test", Values.ofAscii("one"), true)

        assertBytesEquals(byteArray('o', 0, "Test", 0, "one"), objectKey)
    }

    @Test
    fun test02_CompositeKey() {
        val objectKey = DataKeys.getObjectKey("Test", Values.ofAscii("one", "two"), false)

        assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0, "two", 0), objectKey)
    }

    @Test
    fun test03_CompositeKeyPrefix() {
        val objectKey = DataKeys.getObjectKey("Test", Values.ofAscii("one", "two"), true)

        assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0, "two"), objectKey)
    }

    @Test
    fun test04_NullKey() {
        val objectKey = DataKeys.getObjectKey("Test", null, false)

        assertBytesEquals(byteArray('o', 0, "Test", 0), objectKey)
    }

    @Test
    fun test10_KeyType() {
        val objectKey = DataKeys.getObjectKey("Test", Values.ofAscii("one", "two"), false)

        val type = DataKeys.getKeyType(objectKey)

        assertBytesEquals(byteArray("Test"), type)
    }

    @Test
    fun test11_KeyID() {
        val objectKey = DataKeys.getObjectKey("Test", Values.ofAscii("one", "two"), false)

        val id = DataKeys.getObjectKeyID(objectKey)

        assertBytesEquals(byteArray("one", 0, "two", 0), id)
    }

    @Test
    fun test20_SimpleIndexKey() {
        val objectKey = DataKeys.getObjectKey("Test", Values.ofAscii("one"), false)

        val indexKey = DataKeys.getIndexKey(objectKey, "Symbols", Values.ofAscii("alpha"))

        assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "one", 0), indexKey)
    }

    @Test
    fun test21_CompositeIndexKey() {
        val objectKey = DataKeys.getObjectKey("Test", Values.ofAscii("one", "two"), false)

        val indexKey = DataKeys.getIndexKey(objectKey, "Symbols", Values.ofAscii("alpha", "beta"))

        assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "one", 0, "two", 0), indexKey)
    }

    @Test
    fun test30_SimpleIndexKeyStart() {
        val indexKeyStart = DataKeys.getIndexKeyStart("Test", "Symbols", Values.ofAscii("alpha"), false)

        assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0), indexKeyStart)
    }

    @Test
    fun test31_SimpleIndexKeyStartPrefix() {
        val indexKeyStart = DataKeys.getIndexKeyStart("Test", "Symbols", Values.ofAscii("alpha"), true)

        assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha"), indexKeyStart)
    }

    @Test
    fun test32_CompositeIndexKeyStart() {
        val indexKeyStart = DataKeys.getIndexKeyStart("Test", "Symbols", Values.ofAscii("alpha", "beta"), false)

        assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0), indexKeyStart)
    }

    @Test
    fun test33_CompositeIndexKeyStartPrefix() {
        val indexKeyStart = DataKeys.getIndexKeyStart("Test", "Symbols", Values.ofAscii("alpha", "beta"), true)

        assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta"), indexKeyStart)
    }

    @Test
    fun test34_NullIndexPrefix() {
        val indexPrefix = DataKeys.getIndexKeyStart("Test", "Symbols", null, false)

        assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0), indexPrefix)
    }

    @Test
    fun test40_IndexName() {
        val objectKey = DataKeys.getObjectKey("Test", Values.ofAscii("one"), false)
        val indexKey = DataKeys.getIndexKey(objectKey, "Symbols", Values.ofAscii("alpha", "beta"))

        assertBytesEquals(byteArray("Symbols"), DataKeys.getIndexKeyName(indexKey))
    }
}
