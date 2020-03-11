package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.model.findById
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_02_IDs : ModelDBTests() {

    @Test
    fun test00_FindByPk() {
        val salomon = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(salomon)
        mdb.put(laila)
        mdb.put(Adult("Someone", "Somewhere", Date(1, 1, 1990)))

        mdb.findById<Adult>("BRYS").use { cursor ->
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(laila, it.model)
                assertNotSame(laila, it.model)
            }
            cursor.next()
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(salomon, it.model)
                assertNotSame(salomon, it.model)
            }
            cursor.next()
            assertFalse(cursor.isValid())
        }
    }

    @Test
    fun test01_FindNothingByPk() {
        mdb.put(Adult("Salomon", "BRYS", Date(15, 12, 1986)))
        mdb.put(Adult("Laila", "BRYS", Date(25, 8, 1989)))

        mdb.findById<Adult>("BRY").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test02_FindByPkOpen() {
        val salomon = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(salomon)
        mdb.put(laila)
        mdb.put(Adult("Someone", "Somewhere", Date(1, 1, 1990)))

        mdb.findById<Adult>("BRY", isOpen = true).use { cursor ->
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(laila, it.model)
                assertNotSame(laila, it.model)
            }
            cursor.next()
            assertTrue(cursor.isValid())
            cursor.model().also {
                assertEquals(salomon, it.model)
                assertNotSame(salomon, it.model)
            }
            cursor.next()
            assertFalse(cursor.isValid())
        }
    }

}
