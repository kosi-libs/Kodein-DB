package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.test.utils.newBuffer
import org.kodein.memory.Allocation
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

fun baseOptions() = LevelDB.Options(trackClosableAllocation = true)

expect fun platformOptions(): LevelDB.Options

expect val platformFactory: LevelDBFactory

@Suppress("FunctionName")
abstract class LevelDBTests {

    protected var ldb: LevelDB? = null

    open fun options(): LevelDB.Options = platformOptions()

    open val factory: LevelDBFactory = platformFactory

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
