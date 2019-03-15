package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.Allocation
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.test.utils.newBuffer
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

fun baseOptions() = LevelDB.Options(trackClosableAllocation = true)

expect fun platformOptions(): LevelDB.Options

expect val platformFactory: LevelDB.Factory

@Suppress("FunctionName")
abstract class LevelDBTests {

    protected var ldb: LevelDB? = null

    private val buffers = ArrayList<Allocation>()

    protected fun buffer(vararg values: Any) = newBuffer(*values).also { buffers += it }

    open fun options(): LevelDB.Options = platformOptions()

    open val factory: LevelDB.Factory = platformFactory

    @BeforeTest
    fun setUp() {
        factory.destroy("db")
        ldb = factory.open("db", options())
    }

    @AfterTest
    fun tearDown() {
        buffers.forEach { it.close() }
        buffers.clear()
        ldb?.close()
        ldb = null
        factory.destroy("db")
    }
}
