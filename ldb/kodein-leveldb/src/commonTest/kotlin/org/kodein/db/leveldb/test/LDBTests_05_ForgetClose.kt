package org.kodein.db.leveldb.test

import org.kodein.db.test.utils.AssertLogger
import org.kodein.log.Logger
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class LDBTests_05_ForgetClose : LevelDBTests() {

    @Test
    fun test_00_CursorTrack() {
        val logger = AssertLogger()
        ldb!!.close()
        ldb = factory.open("db", options().copy(loggerFactory = { cls -> Logger(cls, logger.filter) }))
        val cursor = ldb!!.newCursor()
        val countBeforeClose = logger.count
        ldb!!.close()
        assertEquals((countBeforeClose + 1).toLong(), logger.count.toLong())
        assertEquals("Cursor must be closed. Creation stack trace:", logger.last!!.msg!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' })
        assertEquals(Logger.Level.WARNING, logger.last!!.level)
        cursor.close()
    }

    @Test
    fun test_01_CursorNoTrack() {
        val logger = AssertLogger()
        ldb!!.close()
        ldb = factory.open("db", options().copy(loggerFactory = { cls -> Logger(cls, logger.filter) }, trackClosableAllocation = false))
        val cursor = ldb!!.newCursor()
        val countBeforeClose = logger.count
        ldb!!.close()
        assertEquals((countBeforeClose + 1).toLong(), logger.count.toLong())
        assertEquals("Cursor has not been properly closed. To track its allocation, open the DB with trackClosableAllocation = true", logger.last!!.msg!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' })
        cursor.close()
    }

}
