package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.default
import org.kodein.db.leveldb.inDir
import org.kodein.db.leveldb.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class LDBTests_00_SimpleOp : LevelDBTests() {

    class LDB : LDBTests_00_SimpleOp() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_00_SimpleOp() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_PutGet() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        val bytes = ldb!!.get(buffer("key"))!!
        assertBytesEquals(byteArray("newValueBuffer"), bytes)
        bytes.close()
    }

    @Test
    fun test_01_BadGet() {
        assertNull(ldb!!.get(buffer("key")))
    }

    @Test
    fun test_02_PutDelete() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        ldb!!.delete(buffer("key"))

        assertNull(ldb!!.get(buffer("key")))
    }

    @Test
    fun test_03_DirectPutGet() {
        ldb!!.put(buffer("key0"), buffer("newValueBuffer0"))
        ldb!!.put(buffer("key1"), buffer("newValueBuffer1"))
        ldb!!.put(buffer("key2"), buffer("newValueBuffer2"))

        val value0 = ldb!!.get(buffer("key0"))!!
        val value1 = ldb!!.get(buffer("key1"))!!
        val value2 = ldb!!.get(buffer("key2"))!!
        assertBytesEquals(byteArray("newValueBuffer0"), value0)
        assertBytesEquals(byteArray("newValueBuffer1"), value1)
        assertBytesEquals(byteArray("newValueBuffer2"), value2)
        value0.close()
        value1.close()
        value2.close()
    }

    @Test
    fun test_04_PutDirectGet() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        val value = ldb!!.get(buffer("key"))!!
        assertBytesEquals(byteArray("newValueBuffer"), value)
        value.close()
    }

    @Test
    fun test_05_PutDirectDelete() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        ldb!!.delete(buffer("key"), LevelDB.WriteOptions(sync = true))

        assertNull(ldb!!.get(buffer("key")))
    }

}