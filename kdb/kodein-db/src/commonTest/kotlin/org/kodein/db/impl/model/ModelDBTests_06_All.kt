package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.model.putAll
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
open class ModelDBTests_06_All : ModelDBTests() {

    override fun testTypeTable() = TypeTable {
        root<Adult>()
        root<Child>()
    }

    @Test
    fun test01_Polymorphism() {

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val her = Adult("Laila", "ATIE", Date(25, 8, 1989))
        val dog = Child("Lana", "Woof", Date(8, 7, 2017), mdb.keyFrom(me) to mdb.keyFrom(her))

        mdb.putAll(listOf(me, her, dog))

        mdb.findAll().use {
            assertTrue(it.isValid())
            assertEquals(her, it.model().model)
            it.next()
            assertTrue(it.isValid())
            assertEquals(me, it.model().model)
            it.next()
            assertTrue(it.isValid())
            assertEquals(dog, it.model().model)
            it.next()
            assertFalse(it.isValid())
        }
    }

}
