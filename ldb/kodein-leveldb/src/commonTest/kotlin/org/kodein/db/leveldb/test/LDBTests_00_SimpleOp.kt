package org.kodein.db.leveldb.test

import kotlinx.io.core.use
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import kotlin.test.Test
import kotlin.test.assertNull

class LDBTests_00_SimpleOp : LevelDBTests() {

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

    @Test
    fun test_06_IndirectGet() {
        ldb!!.put(buffer("one"), buffer("two"))
        ldb!!.put(buffer("two"), buffer("three"))

        val value = ldb!!.indirectGet(buffer("one"))!!
        assertBytesEquals(byteArray("three"), value)
        value.close()
    }

    @Test
    fun test_07_IndirectUnexistingGet() {
        ldb!!.put(buffer("one"), buffer("two"))

        assertNull(ldb!!.indirectGet(buffer("one")))
        assertNull(ldb!!.indirectGet(buffer("two")))
    }

    @Test
    fun test_08_IndirectUnexistingGetInSnapshot() {
        ldb!!.put(buffer("one"), buffer("two"))

        ldb!!.newSnapshot().use { snapshot ->
            ldb!!.put(buffer("two"), buffer("three"))

            assertNull(ldb!!.indirectGet(buffer("one"), LevelDB.ReadOptions(snapshot = snapshot)))
            assertNull(ldb!!.indirectGet(buffer("two"), LevelDB.ReadOptions(snapshot = snapshot)))
        }
    }


}