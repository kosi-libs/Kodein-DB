package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.newHeapKey
import org.kodein.db.model.findAllByIndex
import org.kodein.db.model.findByIndex
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_03_Indexes : ModelDBTests() {

    @Test
    fun test00_FindAllByIndex() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val notMe = Adult("Salomon", "MALHANGU", Date(10, 7, 1956))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(me)
        mdb.put(notMe)
        mdb.put(laila)

        mdb.findAllByIndex<Adult>("firstName").use {
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(laila, it.value)
                assertNotSame(laila, it.value)
            }
            it.next()
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(me, it.value)
                assertNotSame(me, it.value)
            }
            it.next()
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(notMe, it.value)
                assertNotSame(notMe, it.value)
            }
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindByIndexValue() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val notMe = Adult("Salomon", "MALHANGU", Date(10, 7, 1956))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(me)
        mdb.put(notMe)
        mdb.put(laila)

        mdb.findByIndex<Adult>("firstName", Value.ofAscii("Salomon")).use {
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(me, it.value)
                assertNotSame(me, it.value)
            }
            it.next()
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(notMe, it.value)
                assertNotSame(notMe, it.value)
            }
            it.next()
            assertFalse(it.isValid())
        }

    }

    @Test
    fun test02_FindByIndexOpen() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val notMe = Adult("Salomon", "MALHANGU", Date(10, 7, 1956))
        val sarah = Adult("Sarah", "Bernhardt", Date(23, 10, 1844))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(me)
        mdb.put(notMe)
        mdb.put(sarah)
        mdb.put(laila)

        mdb.findByIndex<Adult>("firstName", Value.ofAscii("Sa"), isOpen = true).use {
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(me, it.value)
                assertNotSame(me, it.value)
            }
            it.next()
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(notMe, it.value)
                assertNotSame(notMe, it.value)
            }
            it.next()
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(sarah, it.value)
                assertNotSame(sarah, it.value)
            }
            it.next()
            assertFalse(it.isValid())
        }

    }

    @Test
    fun test03_FindNothingByIndex() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(me)
        mdb.put(laila)

        mdb.findByIndex<Adult>("firstName", Value.ofAscii("Roger")).use {
            assertFalse(it.isValid())
        }

        mdb.findByIndex<Adult>("firstName", Value.ofAscii("R"), isOpen = true).use {
            assertFalse(it.isValid())
        }

    }

    @Test
    fun test04_getIndexes() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        mdb.put(me)

        val indexes = mdb.getIndexesOf(mdb.newHeapKey<Adult>(Value.ofAscii("BRYS", "Salomon"))).toSet()
        assertEquals(setOf("birth", "firstName"), indexes)
    }
}
