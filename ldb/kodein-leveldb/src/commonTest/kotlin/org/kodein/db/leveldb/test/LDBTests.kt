package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.based
import org.kodein.db.leveldb.default
import org.kodein.db.test.utils.newBuffer
import org.kodein.db.test.utils.platformTmpPath
import org.kodein.log.Logger
import org.kodein.log.print.printLogFilter
import org.kodein.memory.io.Allocation
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

fun baseOptions() = LevelDB.Options(trackClosableAllocation = true)

@Suppress("FunctionName")
abstract class LevelDBTests {

    protected var ldb: LevelDB? = null

    open fun options(): LevelDB.Options = baseOptions().copy(loggerFactory = { Logger(it, printLogFilter) })

    open val factory: LevelDBFactory = LevelDB.default.based("$platformTmpPath/")

    private val buffers = ArrayList<Allocation>()

    protected fun buffer(vararg values: Any) = newBuffer(*values).also { buffers += it }

    @BeforeTest
    fun setUp() {
        factory.destroy("db")
        ldb = factory.open("db", options())
    }

    @AfterTest
    fun tearDown() {
        ldb?.close()
        ldb = null
        factory.destroy("db")
    }

    @AfterTest
    fun clearBuffers() {
        buffers.forEach { it.close() }
        buffers.clear()
    }

}
