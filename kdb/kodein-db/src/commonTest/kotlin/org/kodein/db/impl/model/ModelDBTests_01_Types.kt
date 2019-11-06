package org.kodein.db.impl.model

import org.kodein.db.model.findAllByType
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_01_Types : ModelDBTests() {

    @Test
    fun test00_findByType() {
        val salomon = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        mdb.put(salomon)
        mdb.put(laila)
        mdb.put(City("Paris", Location(48.864716, 2.349014), 75000))

        mdb.findAllByType<Adult>().use {
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(laila, it.model)
                assertNotSame(laila, it.model)
            }
            it.next()
            assertTrue(it.isValid())
            it.model().also {
                assertEquals(salomon, it.model)
                assertNotSame(salomon, it.model)
            }
            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test01_findByTypeInEmptyDB() {
        mdb.findAllByType<Adult>().use {
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test02_findByWrongType() {
        mdb.put(City("Paris", Location(48.864716, 2.349014), 75000))
        mdb.findAllByType<Adult>().use {
            assertFalse(it.isValid())
        }
    }

}
