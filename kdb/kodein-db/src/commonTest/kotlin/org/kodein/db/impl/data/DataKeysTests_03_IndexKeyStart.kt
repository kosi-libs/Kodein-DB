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
class DataKeysTests_03_IndexKeyStart {

    @Test
    fun test00_SimpleIndexKeyStart() {
        val size = getIndexKeyStartSize("Test", "Symbols", Value.ofAscii("alpha"))
        assertEquals(size, 21)
        Allocation.native(size).use {
            it.putIndexKeyStart("Test", "Symbols", Value.ofAscii("alpha"))
            it.flip()
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0), it)
        }
    }

    @Test
    fun test01_SimpleIndexKeyStartPrefix() {
        val size = getIndexKeyStartSize("Test", "Symbols", Value.ofAscii("alpha"), isOpen = true)
        assertEquals(size, 20)
        Allocation.native(size).use {
            it.putIndexKeyStart("Test", "Symbols", Value.ofAscii("alpha"), isOpen = true)
            it.flip()
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha"), it)
        }
    }

    @Test
    fun test02_CompositeIndexKeyStart() {
        val size = getIndexKeyStartSize("Test", "Symbols", Value.ofAscii("alpha", "beta"))
        assertEquals(size, 26)
        Allocation.native(size).use {
            it.putIndexKeyStart("Test", "Symbols", Value.ofAscii("alpha", "beta"))
            it.flip()
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta", 0), it)
        }
    }

    @Test
    fun test03_CompositeIndexKeyStartPrefix() {
        val size = getIndexKeyStartSize("Test", "Symbols", Value.ofAscii("alpha", "beta"), isOpen = true)
        assertEquals(size, 25)
        Allocation.native(size).use {
            it.putIndexKeyStart("Test", "Symbols", Value.ofAscii("alpha", "beta"), isOpen = true)
            it.flip()
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0, "alpha", 0, "beta"), it)
        }
    }

    @Test
    fun test04_NullIndexPrefix() {
        val size = getIndexKeyStartSize("Test", "Symbols", null)
        assertEquals(size, 15)
        Allocation.native(size).use {
            it.putIndexKeyStart("Test", "Symbols", null)
            it.flip()
            assertBytesEquals(byteArray('i', 0, "Test", 0, "Symbols", 0), it)
        }
    }

}
