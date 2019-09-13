package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.getHeapKey
import org.kodein.db.model.findAllByType
import org.kodein.db.model.putAll
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_05_Polymorphism : ModelDBTests() {

    override fun testTypeTable() = TypeTable {
        root<Person>()
                .sub<Adult>()
                .sub<Child>()
    }

    @Test
    fun test01_Polymorphism() {
        val gilbert = Adult("Gilbert", "BRYS", Date(1, 9, 1954))
        val veronique = Adult("Véronique", "BRYS", Date(17, 10, 1957))
        val salomon = Child("Salomon", "BRYS", Date(15, 12, 1986), mdb.getHeapKey(gilbert) to mdb.getHeapKey(veronique))
        val maroussia = Child("Maroussia", "BRYS", Date(18, 8, 1988), mdb.getHeapKey(gilbert) to mdb.getHeapKey(veronique))
        val benjamin = Child("Benjamin", "BRYS", Date(23, 6, 1992), mdb.getHeapKey(gilbert) to mdb.getHeapKey(veronique))

        mdb.putAll(listOf(gilbert, veronique, salomon, maroussia, benjamin))

        mdb.findAllByType<Person>().use {
            assertTrue(it.isValid())
            val benji = it.model()
            assertEquals(benjamin, benji.value)
            assertNotSame(benjamin, benji.value)

            it.next()
            assertTrue(it.isValid())
            val guilou = it.model()
            assertEquals(gilbert, guilou.value)
            assertNotSame(gilbert, guilou.value)

            it.next()
            assertTrue(it.isValid())
            val yaya = it.model()
            assertEquals(maroussia, yaya.value)
            assertNotSame(maroussia, yaya.value)

            it.next()
            assertTrue(it.isValid())
            val monmon = it.model()
            assertEquals(salomon, monmon.value)
            assertNotSame(salomon, monmon.value)

            it.next()
            assertTrue(it.isValid())
            val vero = it.model()
            assertEquals(veronique, vero.value)
            assertNotSame(veronique, vero.value)

            it.next()
            assertFalse(it.isValid())
        }
    }

}
