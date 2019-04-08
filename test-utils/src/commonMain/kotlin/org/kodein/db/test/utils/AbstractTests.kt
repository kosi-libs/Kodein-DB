package org.kodein.db.test.utils

import org.kodein.db.leveldb.Allocation
import kotlin.test.AfterTest

abstract class AbstractTests {

    private val buffers = ArrayList<Allocation>()

    protected fun buffer(vararg values: Any) = newBuffer(*values).also { buffers += it }

    @AfterTest
    fun clearBuffers() {
        buffers.forEach { it.close() }
        buffers.clear()
    }

}