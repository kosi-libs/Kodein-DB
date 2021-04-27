package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.default
import org.kodein.db.leveldb.inDir
import org.kodein.db.leveldb.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.array
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class LDBTests_02_Batch : LevelDBTests() {

    class LDB : LDBTests_02_Batch() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_02_Batch() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_PutGet() {
        ldb!!.newWriteBatch().use { batch ->
            batch.put(native(1), native("one"))
            batch.put(native(2), native("two"))

            assertNull(ldb!!.get(native(1)))
            assertNull(ldb!!.get(native(2)))

            ldb!!.write(batch)
        }

        val value1 = ldb!!.get(native(1))!!
        val value2 = ldb!!.get(native(2))!!
        assertBytesEquals(array("one"), value1)
        assertBytesEquals(array("two"), value2)
        value1.close()
        value2.close()
    }

    @Test
    fun test_01_DirectPutGet() {
        ldb!!.newWriteBatch().use { batch ->
            batch.put(native(1), native("one"))
            batch.put(native(2), native("two"))
            batch.put(native(3), native("three"))

            assertNull(ldb!!.get(native(1)))
            assertNull(ldb!!.get(native(2)))
            assertNull(ldb!!.get(native(3)))

            ldb!!.write(batch, LevelDB.WriteOptions(sync = true))
        }

        val value1 = ldb!!.get(native(1))!!
        val value2 = ldb!!.get(native(2))!!
        val value3 = ldb!!.get(native(3))!!
        assertBytesEquals(array("one"), value1)
        assertBytesEquals(array("two"), value2)
        assertBytesEquals(array("three"), value3)
        value1.close()
        value2.close()
        value3.close()
    }

    @Test
    fun test_02_DeleteGet() {
        ldb!!.put(native(1), native("one"))
        ldb!!.put(native(2), native("two"))

        ldb!!.newWriteBatch().use { batch ->
            batch.delete(native(1))
            batch.delete(native(2))

            val value1 = ldb!!.get(native(1))!!
            val value2 = ldb!!.get(native(2))!!
            assertBytesEquals(array("one"), value1)
            assertBytesEquals(array("two"), value2)
            value1.close()
            value2.close()

            ldb!!.write(batch)
        }

        assertNull(ldb!!.get(native(1)))
        assertNull(ldb!!.get(native(2)))
    }

    @Test
    fun test_03_DirectDeleteGet() {
        ldb!!.put(native(1), native("one"))
        ldb!!.put(native(2), native("two"))

        ldb!!.newWriteBatch().use { batch ->
            batch.delete(native(1))
            batch.delete(native(2))

            val value1 = ldb!!.get(native(1))!!
            val value2 = ldb!!.get(native(2))!!
            assertBytesEquals(array("one"), value1)
            assertBytesEquals(array("two"), value2)
            value1.close()
            value2.close()

            ldb!!.write(batch)
        }

        assertNull(ldb!!.get(native(1)))
        assertNull(ldb!!.get(native(2)))
    }


}