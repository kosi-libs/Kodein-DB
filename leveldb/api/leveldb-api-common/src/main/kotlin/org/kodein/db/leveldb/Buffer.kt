package org.kodein.db.leveldb

import kotlinx.io.core.IoBuffer
import kotlinx.io.pool.NoPoolImpl
import kotlinx.io.pool.ObjectPool

expect class Buffer {

    val io: IoBuffer

    companion object {
        val Empty: Buffer
        val EmptyPool: ObjectPool<Buffer>
    }

}

internal object EmptyBufferPoolImpl : NoPoolImpl<Buffer>() {
    override fun borrow() = Buffer.Empty
}
