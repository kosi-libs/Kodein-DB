package org.kodein.db.leveldb.test

import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.PlatformLevelDB
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LDBTests_JVM_00_BadBuffer : LevelDBTests() {

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
    fun test_00_Get() {
        (ldb!! as PlatformLevelDB).get(byteBuffer("key").asReadOnlyBuffer())
    }

    @Test(expected = IllegalStateException::class)
    fun test_01_Put() {
        (ldb!! as PlatformLevelDB).put(byteBuffer("key"), byteBuffer("bad-value").asReadOnlyBuffer())
    }

    @Test(expected = IllegalStateException::class)
    fun test_02_Delete() {
        (ldb!! as PlatformLevelDB).delete(byteBuffer("key").asReadOnlyBuffer())
    }

    @Test(expected = IllegalStateException::class)
    fun test_03_IndirectBadGet() {
        (ldb!! as PlatformLevelDB).indirectGet(byteBuffer("one").asReadOnlyBuffer())
    }

    @Test(expected = IllegalStateException::class)
    fun test_04_BatchPut() {
        ldb!!.newWriteBatch().use { batch ->
            batch.put(byteBuffer(1), byteBuffer("one").asReadOnlyBuffer())
        }
    }

    @Test(expected = IllegalStateException::class)
    fun test_05_BatchDelete() {
        ldb!!.newWriteBatch().use { batch ->
            batch.delete(byteBuffer(1).asReadOnlyBuffer())
        }
    }

    @Test(expected = IllegalStateException::class)
    fun test_06_CursorSeek() {
        ldb!!.newCursor().use { it ->
            it.seekTo(byteBuffer(2).asReadOnlyBuffer())
        }
    }

}
