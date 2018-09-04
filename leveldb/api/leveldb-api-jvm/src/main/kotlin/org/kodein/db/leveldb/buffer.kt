package org.kodein.db.leveldb

import kotlinx.io.core.IoBuffer
import kotlinx.io.pool.ObjectPool
import java.nio.ByteBuffer

actual class Buffer(private val content: ByteBuffer, contentReadable: Boolean = false) {

    actual val io = IoBuffer(content)

    init {
        if (contentReadable)
            io.resetForRead()
    }

    fun toByteBuffer(): ByteBuffer {
        val bb = content.slice()
        bb.position(io.startGap)
        bb.limit(io.capacity - io.endGap)
        return bb
    }

    actual companion object {
        actual val Empty: Buffer = Buffer(ByteBuffer.allocate(0))
        actual val EmptyPool: ObjectPool<Buffer> = EmptyBufferPoolImpl
    }

}
