package org.kodein.db.leveldb

import kotlinx.cinterop.*
import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer

actual interface Bytes {
    actual val buffer: IoBuffer
    val content: CPointer<ByteVar>
    actual fun makeView(): Bytes
}

actual interface Allocation : Bytes, Closeable {
    actual companion object {
        actual fun allocHeapBuffer(capacity: Int): Allocation = allocNativeBuffer(capacity)
        actual fun allocNativeBuffer(capacity: Int): Allocation = CPointerAllocation(nativeHeap.allocArray(capacity), capacity)
    }
}

class CPointerAllocation private constructor(override val content: CPointer<ByteVar>, override val buffer: IoBuffer, contentReadable: Boolean) : Allocation {

    constructor(content: CPointer<ByteVar>, contentCapacity: Int, contentReadable: Boolean = false) : this(content, IoBuffer(content, contentCapacity), contentReadable)

    init {
        if (contentReadable)
            buffer.resetForRead()
    }

    override fun makeView() = CPointerAllocation(content, buffer.makeView(), false)

    override fun hashCode(): Int {
        var h = 1
        val p = buffer.startGap
        for (i in (p + buffer.readRemaining - 1) downTo p)
            h = 31 * h + content[i].toInt()
        return h
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Allocation) return false

        if (buffer.readRemaining != other.buffer.readRemaining)
            return false

        val p = buffer.startGap
        var i = buffer.readRemaining - 1
        var j = other.buffer.readRemaining - 1
        while (i >= p) {
            if (content[i] != other.content[j])
                return false
            --i
            --j
        }

        return true
    }

    override fun close() {
        nativeHeap.free(content)
    }

}
