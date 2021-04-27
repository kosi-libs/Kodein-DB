package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.default
import org.kodein.db.leveldb.inDir
import org.kodein.db.leveldb.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.array
import org.kodein.memory.file.FileSystem
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class LDBTests_00_SimpleOp : LevelDBTests() {

    class LDB : LDBTests_00_SimpleOp() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_00_SimpleOp() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_PutGet() {
        ldb!!.put(native("key"), native("newValueBuffer"))

        val bytes = ldb!!.get(native("key"))!!
        assertBytesEquals(array("newValueBuffer"), bytes)
        bytes.close()
    }

    @Test
    fun test_01_BadGet() {
        assertNull(ldb!!.get(native("key")))
    }

    @Test
    fun test_02_PutDelete() {
        ldb!!.put(native("key"), native("newValueBuffer"))

        ldb!!.delete(native("key"))

        assertNull(ldb!!.get(native("key")))
    }

    @Test
    fun test_03_DirectPutGet() {
        ldb!!.put(native("key0"), native("newValueBuffer0"))
        ldb!!.put(native("key1"), native("newValueBuffer1"))
        ldb!!.put(native("key2"), native("newValueBuffer2"))

        val value0 = ldb!!.get(native("key0"))!!
        val value1 = ldb!!.get(native("key1"))!!
        val value2 = ldb!!.get(native("key2"))!!
        assertBytesEquals(array("newValueBuffer0"), value0)
        assertBytesEquals(array("newValueBuffer1"), value1)
        assertBytesEquals(array("newValueBuffer2"), value2)
        value0.close()
        value1.close()
        value2.close()
    }

    @Test
    fun test_04_PutDirectGet() {
        ldb!!.put(native("key"), native("newValueBuffer"))

        val value = ldb!!.get(native("key"))!!
        assertBytesEquals(array("newValueBuffer"), value)
        value.close()
    }

    @Test
    fun test_05_PutDirectDelete() {
        ldb!!.put(native("key"), native("newValueBuffer"))

        ldb!!.delete(native("key"), LevelDB.WriteOptions(sync = true))

        assertNull(ldb!!.get(native("key")))
    }

}