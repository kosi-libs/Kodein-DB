package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.db.test.utils.ushort
import org.kodein.db.toArrayMemory
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.asMemory
import org.kodein.memory.io.native
import org.kodein.memory.use
import org.kodein.memory.util.deferScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("ClassName")
class DataKeysTests_01_IndexKey {

    @Test
    fun test00_SimpleIndexKey() {
        deferScope {
            val id = Value.of("one")
            val indexSize = getIndexKeySize(id.toArrayMemory(), "Symbols", Value.of("alpha"))
            assertEquals(24, indexSize)

            val indexKey = Allocation.native(indexSize) {
                writeIndexKey(1, id.toArrayMemory(), "Symbols", Value.of("alpha"))
            }.useInScope()
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "one", 0), indexKey)
        }
    }

    @Test
    fun test01_CompositeIndexKey() {
        deferScope {
            val id = Value.of("one", "two")
            val indexSize = getIndexKeySize(id.toArrayMemory(), "Symbols", Value.of("alpha", "beta"))
            assertEquals(33, indexSize)
            val indexKey = Allocation.native(indexSize) {
                writeIndexKey(1, id.toArrayMemory(), "Symbols", Value.of("alpha", "beta"))
            }.useInScope()
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "one", 0, "two", 0), indexKey)
        }
    }

    @Test
    fun test02_SimpleIndexKeyStart() {
        val size = getIndexKeyStartSize("Symbols", Value.of("alpha"))
        assertEquals(size, 20)
        Allocation.native(size) {
            writeIndexKeyStart(1, "Symbols", Value.of("alpha"))
        } .use {
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0), it)
        }
    }

    @Test
    fun test03_SimpleIndexKeyStartPrefix() {
        val size = getIndexKeyStartSize("Symbols", Value.of("alpha"), isOpen = true)
        assertEquals(size, 19)
        Allocation.native(size) {
            writeIndexKeyStart(1, "Symbols", Value.of("alpha"), isOpen = true)
        } .use {
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha"), it)
        }
    }

    @Test
    fun test04_CompositeIndexKeyStart() {
        val size = getIndexKeyStartSize("Symbols", Value.of("alpha", "beta"))
        assertEquals(size, 25)
        Allocation.native(size) {
            writeIndexKeyStart(1, "Symbols", Value.of("alpha", "beta"))
        } .use {
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0), it)
        }
    }

    @Test
    fun test05_CompositeIndexKeyStartPrefix() {
        val size = getIndexKeyStartSize("Symbols", Value.of("alpha", "beta"), isOpen = true)
        assertEquals(size, 24)
        Allocation.native(size) {
            writeIndexKeyStart(1, "Symbols", Value.of("alpha", "beta"), isOpen = true)
        } .use {
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta"), it)
        }
    }

    @Test
    fun test06_NullIndexPrefix() {
        val size = getIndexKeyStartSize("Symbols", null)
        assertEquals(size, 14)
        Allocation.native(size) {
            writeIndexKeyStart(1, "Symbols", null)
        } .use {
            assertBytesEquals(array('i', 0, int(1), "Symbols", 0), it)
        }
    }

    @Test
    fun test07_IndexBodyWithoutMetadata() {
        deferScope {
            val indexBody = Allocation.native(32) {
                writeIndexBody(Value.of("one").toArrayMemory(), Value.of("value"), null)
            }.useInScope()
            assertBytesEquals(array(128, ushort(5), ushort(3)), indexBody)
        }
    }

    @Test
    fun test08_IndexBodyWithMetadata() {
        deferScope {
            val indexBody = Allocation.native(32) {
                writeIndexBody(Value.of("one").toArrayMemory(), Value.of("value"), Value.of("metadata"))
            }.useInScope()
            assertBytesEquals(array(128, ushort(5), ushort(3), "metadata"), indexBody)
        }
    }

    @Test
    fun test09_IndexInfos() {
        deferScope {
            val id = Value.of("One").toArrayMemory()
            val value = Value.of("alpha", "beta")
            val indexKey = Allocation.native(32) {
                writeIndexKey(1, id, "Symbols", value)
            }.useInScope()
            val indexBody = Allocation.native(32) {
                writeIndexBody(id, value, Value.of("Metadata"))
            }.useInScope()
            assertEquals("Symbols", getIndexKeyName(indexKey))
            assertEquals(1, getIndexKeyDocumentType(indexKey))
            assertBytesEquals(array("One"), getIndexDocumentId(indexKey, indexBody))
            assertBytesEquals(array("Metadata"), getIndexBodyAssociatedData(indexBody)!!)
        }
    }

    @Test
    fun test10_IndexV0Infos() {
        val indexKey = array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "one", 0).asMemory()
        val indexBody = array('o', 0, int(1), "one", 0).asMemory()
        assertBytesEquals(array("one"), getIndexDocumentId(indexKey, indexBody))
        assertNull(getIndexBodyAssociatedData(indexBody))
    }
}
