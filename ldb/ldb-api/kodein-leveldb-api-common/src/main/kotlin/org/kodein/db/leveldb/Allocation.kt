package org.kodein.db.leveldb

import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer

expect interface Bytes {
    val buffer: IoBuffer
    fun makeView(): Bytes
}

expect interface Allocation : Bytes, Closeable {

    companion object {
        // /!\ Close is still needed!
        fun allocHeapBuffer(capacity: Int): Allocation

        fun allocNativeBuffer(capacity: Int): Allocation
    }
}

fun Bytes.toAllocation(): Allocation = object : Allocation, Bytes by this {
    override fun close() {}
}
