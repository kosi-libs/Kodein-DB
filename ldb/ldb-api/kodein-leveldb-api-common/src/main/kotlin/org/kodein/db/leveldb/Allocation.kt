package org.kodein.db.leveldb

import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer

expect class Allocation : Closeable {

    val buffer: IoBuffer

    companion object {
        // /!\ Close is still needed!
        fun allocHeapBuffer(capacity: Int): Allocation

        fun allocNativeBuffer(capacity: Int): Allocation
    }
}
