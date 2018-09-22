package org.kodein.db.leveldb

import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import java.nio.ByteBuffer

actual interface Bytes {
    actual val buffer: IoBuffer
    fun byteBuffer(): ByteBuffer
    actual fun makeView(): Bytes
}

actual interface Allocation : Bytes, Closeable {
    actual companion object {
        actual fun allocHeapBuffer(capacity: Int): Allocation = ByteBufferAllocation(ByteBuffer.allocate(capacity))
        actual fun allocNativeBuffer(capacity: Int): Allocation = ByteBufferAllocation(ByteBuffer.allocateDirect(capacity))
    }
}

class ByteBufferAllocation private constructor(private val content: ByteBuffer, override val buffer: IoBuffer, contentReadable: Boolean) : Allocation {

    constructor(content: ByteBuffer, contentReadable: Boolean = false) : this(content, IoBuffer(content), contentReadable)

    init {
        if (contentReadable)
            buffer.resetForRead()
    }

    override fun byteBuffer(): ByteBuffer {
        val bb = content.duplicate()
        bb.position(buffer.startGap)
        bb.limit(buffer.capacity - buffer.endGap)
        return bb
    }

    override fun makeView() = ByteBufferAllocation(content, buffer.makeView(), false)

    override fun hashCode() = content.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Allocation) return false

        if (content != other.byteBuffer()) return false

        return true
    }

    override fun close() {}

}
