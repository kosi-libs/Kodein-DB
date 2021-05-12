package org.kodein.db.impl.model

import org.kodein.db.Middleware
import org.kodein.db.encryption.Encryption
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.findAllByType
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
abstract class ModelDBTests_01_Types : ModelDBTests() {

    class LDB : ModelDBTests_01_Types() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_01_Types() { override val factory = ModelDB.inMemory }

    abstract class Encrypted : ModelDBTests_01_Types(), ModelDBTests.Encrypted {
        class LDB : Encrypted() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
        class IM : Encrypted() { override val factory = ModelDB.inMemory }
    }


    @Test
    fun test00_findByType() {
        val salomon = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val laila = Adult("Laila", "BRYS", Date(25, 8, 1989))
        val salomonKey = mdb.put(salomon).key
        val lailaKey = mdb.put(laila).key
        mdb.put(City("Paris", Location(48.864716, 2.349014), 75000))

        mdb.findAllByType<Adult>().use { cursor ->
            assertCursorIs(cursor) {
                lailaKey {
                    assertEquals(laila, it.model().model)
                    assertNotSame(laila, it.model().model)
                }
                salomonKey {
                    assertEquals(salomon, it.model().model)
                    assertNotSame(salomon, it.model().model)
                }
            }
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
