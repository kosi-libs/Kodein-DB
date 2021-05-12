package org.kodein.db.impl.model

import org.kodein.db.Middleware
import org.kodein.db.encryption.DBFeatureDisabledError
import org.kodein.db.encryption.Encryption
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.findAllByIndex
import org.kodein.db.model.get
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
abstract class ModelDBTests_04_Refs : ModelDBTests() {

    class LDB : ModelDBTests_04_Refs(), ModelDBTests.LDB
    class IM : ModelDBTests_04_Refs(), ModelDBTests.IM

    abstract class Encrypted : ModelDBTests_04_Refs(), ModelDBTests.Encrypted {
        class LDB : Encrypted(), ModelDBTests.LDB
        class IM : Encrypted(), ModelDBTests.IM
    }


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

        val meBirth = mdb.put(Birth(meKey, mdb.keyFrom(sjeg))).key
        val lailaBirth = mdb.put(Birth(lailaKey, mdb.keyFrom(pap))).key

        mdb.findAllByIndex<Birth>("date").use { cursor ->
            assertCursorIs(cursor) {
                meBirth {
                    val otherMe = mdb[it.model().model.adult]
                    assertEquals(me, otherMe?.model)
                    assertNotSame(me, otherMe?.model)
                    val otherSjeg = mdb[it.model().model.city]
                    assertEquals(sjeg, otherSjeg?.model)
                    assertNotSame(sjeg, otherSjeg?.model)
                }
                lailaBirth {
                    val otherLaila = mdb[it.model().model.adult]
                    assertEquals(laila, otherLaila?.model)
                    assertNotSame(laila, otherLaila?.model)
                    val otherPap = mdb[it.model().model.city]
                    assertEquals(pap, otherPap?.model)
                    assertNotSame(pap, otherPap?.model)
                }
            }
        }
    }

}
