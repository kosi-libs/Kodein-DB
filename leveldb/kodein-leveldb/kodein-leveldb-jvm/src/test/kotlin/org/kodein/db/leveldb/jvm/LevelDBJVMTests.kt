package org.kodein.db.leveldb.jvm

import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.PlatformLevelDB
import org.kodein.db.leveldb.test.LevelDBTests
import org.kodein.log.Logger
import org.kodein.log.print.printLogFilter
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LevelDBJVMTests : LevelDBTests(LDBJVMTestsFactory) {

    private object LDBJVMTestsFactory : LevelDB.Factory {

        val prefix: String = File.createTempFile("kodein-db-leveldb-", null).absolutePath

        override fun open(path: String, options: LevelDB.Options) = LevelDBJVM.open("$prefix.$path.ldb", options)

        override fun destroy(path: String) = LevelDBJVM.destroy("$prefix.$path.ldb")
    }

    override fun basicOptions(): LevelDB.Options =
            super.basicOptions().copy(loggerFactory = { Logger(it, printLogFilter) })


    fun byteArray(vararg values: Any): ByteArray {
        val baos = ByteArrayOutputStream()
        for (value in values) {
            when (value) {
                is Number -> baos.write(value.toByte().toInt())
                is Char -> baos.write(value.toByte().toInt())
                is String -> {
                    for (i in 0 until value.length)
                        baos.write(value[i].toByte().toInt())
                }
                else -> throw IllegalArgumentException(value.javaClass.name)
            }
        }
        return baos.toByteArray()
    }

    fun byteBuffer(vararg values: Any): ByteBuffer = ByteBuffer.wrap(byteArray(*values))


    @Test(expected = IllegalStateException::class)
    fun test_90_BadBuffer_00_Get() {
        (ldb!! as PlatformLevelDB).get(byteBuffer("key").asReadOnlyBuffer())
    }

    @Test(expected = IllegalStateException::class)
    fun test_90_BadBuffer_01_Put() {
        (ldb!! as PlatformLevelDB).put(byteBuffer("key"), byteBuffer("bad-value").asReadOnlyBuffer())
    }

    @Test(expected = IllegalStateException::class)
    fun test_90_BadBuffer_02_Delete() {
        (ldb!! as PlatformLevelDB).delete(byteBuffer("key").asReadOnlyBuffer())
    }

    @Test(expected = IllegalStateException::class)
    fun test_90_BadBuffer_03_IndirectBadGet() {
        (ldb!! as PlatformLevelDB).indirectGet(byteBuffer("one").asReadOnlyBuffer())
    }

    @Test(expected = IllegalStateException::class)
    fun test_90_BadBuffer_04_BatchPut() {
        ldb!!.newWriteBatch().use { batch ->
            batch.put(byteBuffer(1), byteBuffer("one").asReadOnlyBuffer())
        }
    }

    @Test(expected = IllegalStateException::class)
    fun test_90_BadBuffer_05_BatchDelete() {
        ldb!!.newWriteBatch().use { batch ->
            batch.delete(byteBuffer(1).asReadOnlyBuffer())
        }
    }

    @Test(expected = IllegalStateException::class)
    fun test_90_BadBuffer_06_IteratorSeek() {
        ldb!!.newIterator().use { it ->
            it.seekTo(byteBuffer(2).asReadOnlyBuffer())
        }
    }

}
