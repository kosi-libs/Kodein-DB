package org.kodein.db.impl.model

import org.kodein.db.Middleware
import org.kodein.db.Value
import org.kodein.db.encryption.DBFeatureDisabledError
import org.kodein.db.encryption.Encryption
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.findById
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
abstract class ModelDBTests_02_IDs : ModelDBTests() {

    class LDB : ModelDBTests_02_IDs(), ModelDBTests.LDB
    class IM : ModelDBTests_02_IDs(), ModelDBTests.IM

    @Test
    fun test00_FindByID() {
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
    fun test01_FindNothingByID() {
        mdb.put(Adult("Salomon", "BRYS", Date(15, 12, 1986)))
        mdb.put(Adult("Laila", "BRYS", Date(25, 8, 1989)))

        mdb.findById<Adult>("BRY").use {
            assertFalse(it.isValid())
        }
    }

    @Test
    open fun test02_FindByPkOpen() {
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
