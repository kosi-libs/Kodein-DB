package org.kodein.db.leveldb

import kotlinx.cinterop.*
import kotlinx.io.core.Closeable
import kotlinx.io.core.IoBuffer

actual class Allocation(val content: CPointer<ByteVar>, contentCapacity: Int, contentReadable: Boolean = false) : Closeable {

    actual val buffer = IoBuffer(content, contentCapacity)

    init {
        if (contentReadable)
            buffer.resetForRead()
    }

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

    actual companion object {
        actual fun allocHeapBuffer(capacity: Int) = allocNativeBuffer(capacity)
        actual fun allocNativeBuffer(capacity: Int) = Allocation(nativeHeap.allocArray(capacity), capacity)
    }

    override fun close() {
        nativeHeap.free(content)
    }

}
