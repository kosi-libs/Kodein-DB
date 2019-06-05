package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.model.findByPrimaryKey
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_02_PrimaryKeys : ModelDBTests() {

    @Test
    fun test00_FindByPk() {
        val salomon = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(salomon)
        mdb.put(laila)
        mdb.put(Adult("Someone", "Somewhere", Date(1, 1, 1990)))

        mdb.findByPrimaryKey<Adult>(Value.ofAscii("BRYS")).use {
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(laila, it.value)
                assertNotSame(laila, it.value)
            }
            it.next()
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(salomon, it.value)
                assertNotSame(salomon, it.value)
            }
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_FindNothingByPk() {
        mdb.put(Adult("Salomon", "BRYS", Date(15, 12, 1986)))
        mdb.put(Adult("Laila", "BRYS", Date(25, 8, 1989)))

        mdb.findByPrimaryKey<Adult>(Value.ofAscii("BRY")).use {
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

        mdb.findByPrimaryKey<Adult>(Value.ofAscii("BRY"), isOpen = true).use {
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(laila, it.value)
                assertNotSame(laila, it.value)
            }
            it.next()
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(salomon, it.value)
                assertNotSame(salomon, it.value)
            }
            it.next()
            assertFalse(it.isValid())
        }
    }

}
