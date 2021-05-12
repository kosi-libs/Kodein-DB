package org.kodein.db.impl.model

import org.kodein.db.Middleware
import org.kodein.db.TypeTable
import org.kodein.db.encryption.Encryption
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.ModelDB
import org.kodein.db.model.putAll
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class ModelDBTests_06_All : ModelDBTests() {

    class LDB : ModelDBTests_06_All(), ModelDBTests.LDB
    class IM : ModelDBTests_06_All(), ModelDBTests.LDB

    abstract class Encrypted : ModelDBTests_06_All(), ModelDBTests.Encrypted {
        class LDB : Encrypted(), ModelDBTests.LDB
        class IM : Encrypted(), ModelDBTests.LDB
    }


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

        mdb.findAll().use { cursor ->
            @Suppress("UNCHECKED_CAST")
            assertCursorIs(cursor as ModelCursor<Any>) {
                K(her) {
                    assertEquals(her, it.model().model)
                }
                K(me) {
                    assertEquals(me, it.model().model)
                }
                K(dog) {
                    assertEquals(dog, it.model().model)
                }
            }
        }
    }

}
