package org.kodein.db.leveldb.test

import kotlinx.io.core.use
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBException
import org.kodein.log.Logger
import kotlin.test.*

abstract class LevelDBTests(private val factory: LevelDB.Factory) {

    protected var ldb: LevelDB? = null

    protected open fun basicOptions() = LevelDB.Options(trackClosableAllocation = true)

    private fun buffer(vararg values: Any) = newBuffer(ldb!!, *values)

    @BeforeTest
    fun setUp() {
        factory.destroy("db")
        ldb = factory.open("db", basicOptions())
    }

    @AfterTest
    fun tearDown() {
        ldb?.close()
        ldb = null
        factory.destroy("db")
    }

    @Test
    fun test_00_SimpleOp_00_PutGet() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        assertBytesEquals(byteArray("newValueBuffer"), ldb!!.get(buffer("key"))!!)
    }

    @Test
    fun test_00_SimpleOp_01_BadGet() {
        assertNull(ldb!!.get(buffer("key")))
    }

    @Test
    fun test_00_SimpleOp_02_PutDelete() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        ldb!!.delete(buffer("key"))

        assertNull(ldb!!.get(buffer("key")))
    }

    @Test
    fun test_00_SimpleOp_03_DirectPutGet() {
        ldb!!.put(buffer("key0"), buffer("newValueBuffer0"))
        ldb!!.put(buffer("key1"), buffer("newValueBuffer1"))
        ldb!!.put(buffer("key2"), buffer("newValueBuffer2"))

        assertBytesEquals(byteArray("newValueBuffer0"), ldb!!.get(buffer("key0"))!!)
        assertBytesEquals(byteArray("newValueBuffer1"), ldb!!.get(buffer("key1"))!!)
        assertBytesEquals(byteArray("newValueBuffer2"), ldb!!.get(buffer("key2"))!!)
    }

    @Test
    fun test_00_SimpleOp_04_PutDirectGet() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        assertBytesEquals(byteArray("newValueBuffer"), ldb!!.get(buffer("key"))!!)
    }

    @Test
    fun test_00_SimpleOp_05_PutDirectDelete() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        ldb!!.delete(buffer("key"), LevelDB.WriteOptions(sync = true))

        assertNull(ldb!!.get(buffer("key")))
    }

    @Test
    fun test_00_SimpleOp_06_IndirectGet() {
        ldb!!.put(buffer("one"), buffer("two"))
        ldb!!.put(buffer("two"), buffer("three"))

        assertBytesEquals(byteArray("three"), ldb!!.indirectGet(buffer("one"))!!)
        assertBytesEquals(byteArray("three"), ldb!!.indirectGet(buffer("one"))!!)
    }

    @Test
    fun test_00_SimpleOp_07_IndirectUnexistingGet() {
        ldb!!.put(buffer("one"), buffer("two"))

        assertNull(ldb!!.indirectGet(buffer("one")))
        assertNull(ldb!!.indirectGet(buffer("two")))
    }

    @Test
    fun test_00_SimpleOp_08_IndirectUnexistingGetInSnapshot() {
        ldb!!.put(buffer("one"), buffer("two"))

        ldb!!.newSnapshot().use { snapshot ->
            ldb!!.put(buffer("two"), buffer("three"))

            assertNull(ldb!!.indirectGet(buffer("one"), LevelDB.ReadOptions(snapshot = snapshot)))
            assertNull(ldb!!.indirectGet(buffer("two"), LevelDB.ReadOptions(snapshot = snapshot)))
        }
    }

    @Test
    fun test_01_Snapshot_00_PutGet() {
        ldb!!.newSnapshot().use { snapshot ->
            ldb!!.put(buffer("key"), buffer("newValueBuffer"))

            assertNull(ldb!!.get(buffer("key"), LevelDB.ReadOptions(snapshot = snapshot)))
        }
    }

    @Test
    fun test_01_Snapshot_01_PutDeleteGet() {
        ldb!!.put(buffer("key"), buffer("newValueBuffer"))

        ldb!!.newSnapshot().use { snapshot ->
            ldb!!.delete(buffer("key"))

            assertBytesEquals(byteArray("newValueBuffer"), ldb!!.get(buffer("key"), LevelDB.ReadOptions(snapshot = snapshot))!!)
        }
    }

    @Test
    fun test_02_Batch_00_PutGet() {
        ldb!!.newWriteBatch().use { batch ->
            batch.put(buffer(1), buffer("one"))
            batch.put(buffer(2), buffer("two"))

            assertNull(ldb!!.get(buffer(1)))
            assertNull(ldb!!.get(buffer(2)))

            ldb!!.write(batch)
        }

        assertBytesEquals(byteArray("one"), ldb!!.get(buffer(1))!!)
        assertBytesEquals(byteArray("two"), ldb!!.get(buffer(2))!!)
    }

    @Test
    fun test_02_Batch_01_DirectPutGet() {
        ldb!!.newWriteBatch().use { batch ->
            batch.put(buffer(1), buffer("one"))
            batch.put(buffer(2), buffer("two"))
            batch.put(buffer(3), buffer("three"))

            assertNull(ldb!!.get(buffer(1)))
            assertNull(ldb!!.get(buffer(2)))
            assertNull(ldb!!.get(buffer(3)))

            ldb!!.write(batch, LevelDB.WriteOptions(sync = true))
        }

        assertBytesEquals(byteArray("one"), ldb!!.get(buffer(1))!!)
        assertBytesEquals(byteArray("two"), ldb!!.get(buffer(2))!!)
        assertBytesEquals(byteArray("three"), ldb!!.get(buffer(3))!!)
    }

    @Test
    fun test_02_Batch_02_DeleteGet() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(2), buffer("two"))

        ldb!!.newWriteBatch().use { batch ->
            batch.delete(buffer(1))
            batch.delete(buffer(2))

            assertBytesEquals(byteArray("one"), ldb!!.get(buffer(1))!!)
            assertBytesEquals(byteArray("two"), ldb!!.get(buffer(2))!!)

            ldb!!.write(batch)
        }

        assertNull(ldb!!.get(buffer(1)))
        assertNull(ldb!!.get(buffer(2)))
    }

    @Test
    fun test_02_Batch_03_DirectDeleteGet() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(2), buffer("two"))

        ldb!!.newWriteBatch().use { batch ->
            batch.delete(buffer(1))
            batch.delete(buffer(2))

            assertBytesEquals(byteArray("one"), ldb!!.get(buffer(1))!!)
            assertBytesEquals(byteArray("two"), ldb!!.get(buffer(2))!!)

            ldb!!.write(batch)
        }

        assertNull(ldb!!.get(buffer(1)))
        assertNull(ldb!!.get(buffer(2)))
    }

    @Test
    fun test_03_Iterator_00_Forward() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(2), buffer("two"))

        ldb!!.newIterator().use { it ->
            it.seekToFirst()

            assertTrue(it.isValid())
            assertBytesEquals(byteArray(1), it.transientKey())
            assertBytesEquals(byteArray("one"), it.transientValue())

            it.next()

            assertTrue(it.isValid())
            assertBytesEquals(byteArray(2), it.transientKey())
            assertBytesEquals(byteArray("two"), it.transientValue())

            it.next()

            assertFalse(it.isValid())
        }
    }

    @Test
    fun test_03_Iterator_01_Backward() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(2), buffer("two"))

        ldb!!.newIterator().use { it ->
            it.seekToLast()

            assertTrue(it.isValid())
            assertBytesEquals(byteArray(2), it.transientKey())
            assertBytesEquals(byteArray("two"), it.transientValue())

            it.prev()

            assertTrue(it.isValid())
            assertBytesEquals(byteArray(1), it.transientKey())
            assertBytesEquals(byteArray("one"), it.transientValue())

            it.prev()

            assertFalse(it.isValid())
        }
    }

    @Test
    fun test_03_Iterator_02_Seek() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(3), buffer("three"))

        ldb!!.newIterator().use { it ->
            it.seekTo(buffer(2))

            assertTrue(it.isValid())
            assertBytesEquals(byteArray(3), it.transientKey())
            assertBytesEquals(byteArray("three"), it.transientValue())

            it.next()

            assertFalse(it.isValid())
        }
    }

    @Test
    fun test_03_Iterator_03_DirectSeek() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(3), buffer("three"))

        ldb!!.newIterator().use { it ->
            it.seekTo(buffer(2))

            assertTrue(it.isValid())
            assertBytesEquals(byteArray(3), it.transientKey())
            assertBytesEquals(byteArray("three"), it.transientValue())

            it.next()

            assertFalse(it.isValid())
        }
    }

    @Test()
    fun test_03_Iterator_04_Closed() {
        val it = ldb!!.newIterator()
        it.close()
        assertFailsWith<IllegalStateException> {
            it.seekTo(buffer(0))
        }
    }

    @Test
    fun test_03_Iterator_05_Array() {
        ldb!!.put(buffer("key0"), buffer("value0"))
        ldb!!.put(buffer("key1"), buffer("value1"))

        ldb!!.close()

        ldb = factory.open("db", basicOptions().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        ldb!!.newIterator().use { it ->
            it.seekToFirst()
            assertTrue(it.isValid())

            it.nextArray(10).use { array ->
                assertEquals(2, array.length())
                assertBytesEquals(byteArray("key0"), array.getKey(0))
                assertBytesEquals(byteArray("value0"), array.getValue(0))
                assertBytesEquals(byteArray("key1"), array.getKey(1))
                assertBytesEquals(byteArray("value1"), array.getValue(1))
            }

            assertFalse(it.isValid())
        }
    }

    @Test
    fun test_03_Iterator_06_ArrayFull() {
        ldb!!.put(buffer("key0"), buffer("value0"))
        ldb!!.put(buffer("key1"), buffer("value1"))

        ldb!!.close()

        ldb = factory.open("db", basicOptions().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        ldb!!.newIterator().use { it ->
            it.seekToFirst()
            assertTrue(it.isValid())

            it.nextArray(2, 4).use { array ->
                assertEquals(2, array.length())
                assertBytesEquals(byteArray("key0"), array.getKey(0))
                assertBytesEquals(byteArray("value0"), array.getValue(0))
                assertBytesEquals(byteArray("key1"), array.getKey(1))
                assertBytesEquals(byteArray("value1"), array.getValue(1))
            }

            assertFalse(it.isValid())
        }
    }

    @Test
    fun test_03_Iterator_07_SnapshotForward() {
        ldb!!.put(buffer(1), buffer("one"))
        ldb!!.put(buffer(3), buffer("three"))

        ldb!!.newSnapshot().use { snapshot ->
            ldb!!.put(buffer(2), buffer("three"))

            ldb!!.newIterator(LevelDB.ReadOptions(snapshot = snapshot)).use { it ->
                it.seekToFirst()

                assertTrue(it.isValid())
                assertBytesEquals(byteArray(1), it.transientKey())
                assertBytesEquals(byteArray("one"), it.transientValue())

                it.next()

                assertTrue(it.isValid())
                assertBytesEquals(byteArray(3), it.transientKey())
                assertBytesEquals(byteArray("three"), it.transientValue())

                it.next()

                assertFalse(it.isValid())
            }
        }
    }

    @Test
    fun test_03_Iterator_08_IndirectValue() {
        ldb!!.put(buffer("one"), buffer("two"))
        ldb!!.put(buffer("two"), buffer("three"))

        ldb!!.newIterator().use { it ->
            it.seekTo(buffer("one"))
            assertTrue(it.isValid())

            assertBytesEquals(byteArray("three"), ldb!!.indirectGet(it)!!)
            assertBytesEquals(byteArray("three"), ldb!!.indirectGet(it, LevelDB.ReadOptions())!!)
        }
    }

    @Test
    fun test_03_Iterator_09_IndirectUnexistingValue() {
        ldb!!.put(buffer("one"), buffer("two"))

        ldb!!.newIterator().use { it ->
            it.seekTo(buffer("one"))
            assertTrue(it.isValid())

            assertNull(ldb!!.indirectGet(it))
            assertNull(ldb!!.indirectGet(it, LevelDB.ReadOptions()))
        }
    }

    @Test
    fun test_03_Iterator_10_PutInside() {
        ldb!!.put(buffer("A"), buffer("A"))
        ldb!!.put(buffer("C"), buffer("C"))

        ldb!!.newIterator().use { it ->
            it.seekToFirst()
            assertTrue(it.isValid())
            assertBytesEquals(byteArray("A"), it.transientKey())

            ldb!!.put(buffer("B"), buffer("B"), LevelDB.WriteOptions(sync = true))

            it.next()
            assertTrue(it.isValid())
            assertBytesEquals(byteArray("C"), it.transientKey())

            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test_04_ReOpen_00_PutCloseOpenGet() {
        ldb!!.put(buffer("key"), buffer("value"))

        ldb!!.close()

        ldb = factory.open("db", basicOptions().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        assertBytesEquals(byteArray("value"), ldb!!.get(buffer("key"))!!)
    }

    @Test
    fun test_04_ReOpen_01_PutCloseOpenIter() {
        ldb!!.put(buffer("key"), buffer("value"))

        ldb!!.close()

        ldb = factory.open("db", basicOptions().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        ldb!!.newIterator().use { it ->
            it.seekToFirst()

            assertTrue(it.isValid())
            assertBytesEquals(byteArray("key"), it.transientKey())
            assertBytesEquals(byteArray("value"), it.transientValue())

            it.next()
            assertFalse(it.isValid())
        }
    }

    @Test
    fun test_05_ForgetClose_00_IteratorTrack() {
        val logger = AssertLogger()
        ldb!!.close()
        ldb = factory.open("db", basicOptions().copy(loggerFactory = { cls -> Logger(cls, logger.filter) }))
        val iterator = ldb!!.newIterator()
        val countBeforeClose = logger.count
        ldb!!.close()
        assertEquals((countBeforeClose + 1).toLong(), logger.count.toLong())
        assertEquals("Iterator must be closed. Creation stack trace:", logger.last!!.msg!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' })
        assertEquals(Logger.Level.WARNING, logger.last!!.level)
        iterator.close()
    }

    @Test
    fun test_05_ForgetClose_01_IteratorNoTrack() {
        val logger = AssertLogger()
        ldb!!.close()
        ldb = factory.open("db", basicOptions().copy(loggerFactory = { cls -> Logger(cls, logger.filter) }, trackClosableAllocation = false))
        val iterator = ldb!!.newIterator()
        val countBeforeClose = logger.count
        ldb!!.close()
        assertEquals((countBeforeClose + 1).toLong(), logger.count.toLong())
        assertEquals("Iterator has not been properly closed. To track its allocation, open the DB with trackClosableAllocation = true", logger.last!!.msg!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' })
        iterator.close()
    }

    @Test
    fun test_06_OpenPolicy_00_OpenInexisting() {
        assertFailsWith<LevelDBException> {
            try {
                factory.open("none", basicOptions().copy(openPolicy = LevelDB.OpenPolicy.OPEN))
            } catch (ex: LevelDBException) {
                factory.destroy("none")
                throw ex
            }
        }
    }

    @Test
    fun test_06_OpenPolicy_01_ForceCreate() {
        factory.open("new", basicOptions().copy(openPolicy = LevelDB.OpenPolicy.CREATE)).close()
        factory.destroy("new")
    }

    @Test
    fun test_06_OpenPolicy_02_ForceCreateExisting() {
        assertFailsWith<LevelDBException> {
            factory.open("db", basicOptions().copy(openPolicy = LevelDB.OpenPolicy.CREATE))
        }
    }
}
