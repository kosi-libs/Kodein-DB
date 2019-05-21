package org.kodein.db.impl.model

import org.kodein.db.model.findAllByIndex
import org.kodein.db.orm.kotlinx.asRef
import org.kodein.db.orm.kotlinx.get
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_04_Refs : ModelDBTests() {

    @Test
    fun test00_Refs() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        val meKey = mdb.putAndGetKey(me)
        val lailaKey = mdb.putAndGetKey(laila)

        val sjeg = City("Saint Julien En Genevois", Location(46.1443, 6.0826), 74160)
        val pap = City("Pointe À Pitre", Location(16.2333, -61.5167), 97110)
        mdb.put(sjeg)
        mdb.put(pap)

        mdb.put(Birth(meKey.asRef(), mdb.getKey(sjeg).asRef()))
        mdb.put(Birth(lailaKey.asRef(), mdb.getKey(pap).asRef()))

        mdb.findAllByIndex<Birth>("date").use {
            assertTrue(it.isValid())
            val otherMe = mdb[it.model().adult]
            assertEquals(me, otherMe)
            assertNotSame(me, otherMe)
            val otherSjeg = mdb[it.model().city]
            assertEquals(sjeg, otherSjeg)
            assertNotSame(sjeg, otherSjeg)

            it.next()
            assertTrue(it.isValid())
            val otherLaila = mdb[it.model().adult]
            assertEquals(laila, otherLaila)
            assertNotSame(laila, otherLaila)
            val otherPap = mdb[it.model().city]
            assertEquals(pap, otherPap)
            assertNotSame(pap, otherPap)

            it.next()
            assertFalse(it.isValid())
        }
    }

}
