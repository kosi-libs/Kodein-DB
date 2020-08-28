package org.kodein.db.impl.model

import org.kodein.db.model.findAllByIndex
import org.kodein.db.model.get
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_04_Refs : ModelDBTests() {

    @Test
    fun test00_Refs() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        val meKey = mdb.keyFrom(me)
        mdb.put(meKey, me)
        val lailaKey = mdb.keyFrom(laila)
        mdb.put(lailaKey, laila)

        val sjeg = City("Saint Julien En Genevois", Location(46.1443, 6.0826), 74160)
        val pap = City("Pointe À Pitre", Location(16.2333, -61.5167), 97110)
        mdb.put(sjeg)
        mdb.put(pap)

        mdb.put(Birth(meKey, mdb.keyFrom(sjeg)))
        mdb.put(Birth(lailaKey, mdb.keyFrom(pap)))

        mdb.findAllByIndex<Birth>("date").use {
            assertTrue(it.isValid())
            val otherMe = mdb[it.model().model.adult]
            assertEquals(me, otherMe?.model)
            assertNotSame(me, otherMe?.model)
            val otherSjeg = mdb[it.model().model.city]
            assertEquals(sjeg, otherSjeg?.model)
            assertNotSame(sjeg, otherSjeg?.model)

            it.next()
            assertTrue(it.isValid())
            val otherLaila = mdb[it.model().model.adult]
            assertEquals(laila, otherLaila?.model)
            assertNotSame(laila, otherLaila?.model)
            val otherPap = mdb[it.model().model.city]
            assertEquals(pap, otherPap?.model)
            assertNotSame(pap, otherPap?.model)

            it.next()
            assertFalse(it.isValid())
        }
    }

}
