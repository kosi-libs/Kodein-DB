package org.kodein.db.leveldb

import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer
import java.nio.ByteBuffer

actual class Allocation(private val content: ByteBuffer, contentReadable: Boolean = false) : Closeable {

    actual val buffer = IoBuffer(content)

    init {
        if (contentReadable)
            buffer.resetForRead()
    }

    fun toByteBuffer(): ByteBuffer {
        val bb = content.slice()
        bb.position(buffer.startGap)
        bb.limit(buffer.capacity - buffer.endGap)
        return bb
    }

    override fun hashCode() = content.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Allocation) return false

        if (content != other.content) return false

        return true
    }

    actual companion object {
        actual fun allocHeapBuffer(capacity: Int): Allocation = Allocation(ByteBuffer.allocate(capacity))
        actual fun allocNativeBuffer(capacity: Int): Allocation = Allocation(ByteBuffer.allocateDirect(capacity))
    }

    override fun close() {}

}
