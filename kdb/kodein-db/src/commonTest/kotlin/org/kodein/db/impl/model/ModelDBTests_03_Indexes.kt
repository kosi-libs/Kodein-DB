package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.model.findAllByIndex
import org.kodein.db.model.findByIndex
import org.kodein.db.key
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

        mdb.findAllByIndex<Adult>("firstName").use { cursor ->
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(laila, it.model)
                assertNotSame(laila, it.model)
            }
            cursor.next()
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(me, it.model)
                assertNotSame(me, it.model)
            }
            cursor.next()
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(notMe, it.model)
                assertNotSame(notMe, it.model)
            }
            cursor.next()
            assertFalse(cursor.isValid())
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

        mdb.findByIndex<Adult>("firstName", "Salomon").use { cursor ->
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(me, it.model)
                assertNotSame(me, it.model)
            }
            cursor.next()
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(notMe, it.model)
                assertNotSame(notMe, it.model)
            }
            cursor.next()
            assertFalse(cursor.isValid())
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

        mdb.findByIndex<Adult>("firstName", "Sa", isOpen = true).use { cursor ->
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(me, it.model)
                assertNotSame(me, it.model)
            }
            cursor.next()
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(notMe, it.model)
                assertNotSame(notMe, it.model)
            }
            cursor.next()
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(sarah, it.model)
                assertNotSame(sarah, it.model)
            }
            cursor.next()
            assertFalse(cursor.isValid())
        }

    }

    @Test
    fun test03_FindNothingByIndex() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(me)
        mdb.put(laila)

        mdb.findByIndex<Adult>("firstName", "Roger").use {
            assertFalse(it.isValid())
        }

        mdb.findByIndex<Adult>("firstName", "R", isOpen = true).use {
            assertFalse(it.isValid())
        }

    }

    @Test
    fun test04_getIndexes() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        mdb.put(me)

        val indexes = mdb.getIndexesOf(mdb.key<Adult>(Value.ofAscii("BRYS", "Salomon"))).toSet()
        assertEquals(setOf("birth", "firstName"), indexes)
    }
}
