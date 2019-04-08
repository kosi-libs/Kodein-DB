//package org.kodein.db.impl.data
//
//import kotlinx.io.core.use
//import org.kodein.db.Value
//import org.kodein.db.leveldb.Allocation
//import org.kodein.db.test.utils.assertBytesEquals
//import org.kodein.db.test.utils.byteArray
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//@Suppress("FunctionName")
//class DataKeysTests {
//
//    @Test
//    fun test00_SimpleKey() {
//        val size = getObjectKeySize("Test", Value.ofAscii("one"))
//        assertEquals(size, 11)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeObjectKey("Test", Value.ofAscii("one"))
//            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0), it)
//        }
//    }
//
//    @Test
//    fun test01_SimpleKeyPrefix() {
//        val size = getObjectKeySize("Test", Value.ofAscii("one"), isOpen = true)
//        assertEquals(size, 10)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeObjectKey("Test", Value.ofAscii("one"), isOpen = true)
//            assertBytesEquals(byteArray('o', 0, "Test", 0, "one"), it)
//        }
//    }
//
//    @Test
//    fun test02_CompositeKey() {
//        val size = getObjectKeySize("Test", Value.ofAscii("one", "two"))
//        assertEquals(size, 15)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeObjectKey("Test", Value.ofAscii("one", "two"))
//            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0, "two", 0), it)
//        }
//    }
//
//    @Test
//    fun test03_CompositeKeyPrefix() {
//        val size = getObjectKeySize("Test", Value.ofAscii("one", "two"), isOpen = true)
//        assertEquals(size, 14)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeObjectKey("Test", Value.ofAscii("one", "two"), isOpen = true)
//            assertBytesEquals(byteArray('o', 0, "Test", 0, "one", 0, "two"), it)
//        }
//    }
//
//    @Test
//    fun test04_NullKey() {
//        val size = getObjectKeySize("Test", null)
//        assertEquals(size, 7)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeObjectKey("Test", null)
//            assertBytesEquals(byteArray('o', 0, "Test", 0), it)
//        }
//    }
//
//    @Test
//    fun test10_KeyType() {
//        Allocation.allocHeapBuffer(32).use {
//            it.writeObjectKey("Test", Value.ofAscii("one", "two"))
//            val type = getObjectKeyType(it)
//            assertBytesEquals(byteArray("Test"), type)
//        }
//    }
//
//    @Test
//    fun test11_KeyID() {
//        Allocation.allocHeapBuffer(32).use {
//            it.writeObjectKey("Test", Value.ofAscii("one", "two"))
//            val id = getObjectKeyID(it)
//            assertBytesEquals(byteArray("one", 0, "two", 0), id)
//        }
//    }
//
//    @Test
//    fun test20_SimpleIndexKey() {
//        Allocation.allocHeapBuffer(32).use { objectKey ->
//            objectKey.writeObjectKey("Test", Value.ofAscii("one"))
//
//            val indexSize = getIndexKeySize(objectKey, "Symbols", Value.ofAscii("alpha"))
//            assertEquals(25, indexSize)
//            Allocation.allocHeapBuffer(indexSize).use { index ->
//                index.writeIndexKey(objectKey, "Symbols", Value.ofAscii("alpha"))
//                assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "one", 0), index)
//            }
//        }
//    }
//
//    @Test
//    fun test21_CompositeIndexKey() {
//        Allocation.allocHeapBuffer(32).use { objectKey ->
//            objectKey.writeObjectKey("Test", Value.ofAscii("one", "two"))
//
//            val indexSize = getIndexKeySize(objectKey, "Symbols", Value.ofAscii("alpha", "beta"))
//            assertEquals(34, indexSize)
//            Allocation.allocHeapBuffer(indexSize).use { index ->
//                index.writeIndexKey(objectKey, "Symbols", Value.ofAscii("alpha", "beta"))
//                assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0, "one", 0, "two", 0), index)
//            }
//        }
//    }
//
//    @Test
//    fun test30_SimpleIndexKeyStart() {
//        val size = getIndexKeyStartSize("Test", "Symbols", Value.ofAscii("alpha"))
//        assertEquals(size, 21)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeIndexKeyStart("Test", "Symbols", Value.ofAscii("alpha"))
//            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0), it)
//        }
//    }
//
//    @Test
//    fun test31_SimpleIndexKeyStartPrefix() {
//        val size = getIndexKeyStartSize("Test", "Symbols", Value.ofAscii("alpha"), isOpen = true)
//        assertEquals(size, 20)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeIndexKeyStart("Test", "Symbols", Value.ofAscii("alpha"), isOpen = true)
//            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha"), it)
//        }
//    }
//
//    @Test
//    fun test32_CompositeIndexKeyStart() {
//        val size = getIndexKeyStartSize("Test", "Symbols", Value.ofAscii("alpha", "beta"))
//        assertEquals(size, 26)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeIndexKeyStart("Test", "Symbols", Value.ofAscii("alpha", "beta"))
//            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0), it)
//        }
//    }
//
//    @Test
//    fun test33_CompositeIndexKeyStartPrefix() {
//        val size = getIndexKeyStartSize("Test", "Symbols", Value.ofAscii("alpha", "beta"), isOpen = true)
//        assertEquals(size, 25)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeIndexKeyStart("Test", "Symbols", Value.ofAscii("alpha", "beta"), isOpen = true)
//            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta"), it)
//        }
//    }
//
//    @Test
//    fun test34_NullIndexPrefix() {
//        val size = getIndexKeyStartSize("Test", "Symbols", null)
//        assertEquals(size, 15)
//        Allocation.allocHeapBuffer(size).use {
//            it.writeIndexKeyStart("Test", "Symbols", null)
//            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0), it)
//        }
//    }
//
//    @Test
//    fun test40_IndexName() {
//        Allocation.allocHeapBuffer(32).use { objectKey ->
//            objectKey.writeObjectKey("Test", Value.ofAscii("one"))
//            Allocation.allocHeapBuffer(32).use { indexKey ->
//                indexKey.writeIndexKey(objectKey, "Symbols", Value.ofAscii("alpha", "beta"))
//                assertBytesEquals(byteArray("Symbols"), getIndexKeyName(indexKey))
//            }
//        }
//    }
//}
