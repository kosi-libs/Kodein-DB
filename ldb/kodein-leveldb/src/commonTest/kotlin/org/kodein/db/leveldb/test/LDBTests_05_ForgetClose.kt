package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.default
import org.kodein.db.leveldb.inDir
import org.kodein.db.leveldb.inmemory.inMemory
import org.kodein.db.test.utils.AssertLogger
import org.kodein.log.Logger
import org.kodein.log.LoggerFactory
import org.kodein.memory.file.FileSystem
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
abstract class LDBTests_05_ForgetClose : LevelDBTests() {

    class LDB : LDBTests_05_ForgetClose() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_05_ForgetClose() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_CursorTrack() {
        val logger = AssertLogger()
        ldb!!.close()
        ldb = factory.open("db", options().copy(loggerFactory = LoggerFactory(logger.frontEnd)))
        val cursor = ldb!!.newCursor()
        val countBeforeClose = logger.entries.count()
        ldb!!.close()
        assertEquals((countBeforeClose + 1).toLong(), logger.entries.count().toLong())
        assertEquals("Cursor must be closed. Creation stack trace:", logger.entries.last().third!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' })
        assertEquals(Logger.Level.WARNING, logger.entries.last().second.level)
        cursor.close()
    }

    @Test
    fun test_01_CursorNoTrack() {
        val logger = AssertLogger()
        ldb!!.close()
        ldb = factory.open("db", options().copy(loggerFactory = LoggerFactory(logger.frontEnd), trackClosableAllocation = false))
        val cursor = ldb!!.newCursor()
        val countBeforeClose = logger.entries.count()
        ldb!!.close()
        assertEquals((countBeforeClose + 1).toLong(), logger.entries.count().toLong())
        assertEquals("Cursor has not been properly closed. To track its allocation, set trackClosableAllocation.", logger.entries.last().third!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' })
        cursor.close()
    }

}
