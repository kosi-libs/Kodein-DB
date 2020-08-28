package org.kodein.db.impl.model

import org.kodein.db.TypeTable
import org.kodein.db.model.findAllByType
import org.kodein.db.model.putAll
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_05_PolymorphicTypeTable : ModelDBTests() {

    override fun testTypeTable() = TypeTable {
        root<Person>()
                .sub<Adult>()
                .sub<Child>()
    }

    @Test
    fun test00_PolymorphicTypeTable() {
        val gilbert = Adult("Gilbert", "BRYS", Date(1, 9, 1954))
        val veronique = Adult("Véronique", "BRYS", Date(17, 10, 1957))
        val salomon = Child("Salomon", "BRYS", Date(15, 12, 1986), mdb.keyFrom(gilbert) to mdb.keyFrom(veronique))
        val maroussia = Child("Maroussia", "BRYS", Date(18, 8, 1988), mdb.keyFrom(gilbert) to mdb.keyFrom(veronique))
        val benjamin = Child("Benjamin", "BRYS", Date(23, 6, 1992), mdb.keyFrom(gilbert) to mdb.keyFrom(veronique))

        mdb.putAll(listOf(gilbert, veronique, salomon, maroussia, benjamin))

        mdb.findAllByType<Person>().use {
            assertTrue(it.isValid())
            val benji = it.model()
            assertEquals(benjamin, benji.model)
            assertNotSame(benjamin, benji.model)

            it.next()
            assertTrue(it.isValid())
            val guilou = it.model()
            assertEquals(gilbert, guilou.model)
            assertNotSame(gilbert, guilou.model)

            it.next()
            assertTrue(it.isValid())
            val yaya = it.model()
            assertEquals(maroussia, yaya.model)
            assertNotSame(maroussia, yaya.model)

            it.next()
            assertTrue(it.isValid())
            val monmon = it.model()
            assertEquals(salomon, monmon.model)
            assertNotSame(salomon, monmon.model)

            it.next()
            assertTrue(it.isValid())
            val vero = it.model()
            assertEquals(veronique, vero.model)
            assertNotSame(veronique, vero.model)

            it.next()
            assertFalse(it.isValid())
        }
    }

}
