@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db

import org.kodein.memory.Closeable
import org.kodein.memory.io.*


sealed class Key<out T : Any>(val bytes: ReadBuffer) {
    abstract fun asHeapKey(): Key<T>
    class Heap<out T : Any>(bytes: ReadBuffer) : Key<T>(bytes) {
        override fun asHeapKey(): Key<T> = this
    }
    class Native<out T : Any>(private val alloc: Allocation) : Key<T>(alloc), Closeable {
        override fun close() { alloc.close() }
        override fun asHeapKey(): Key<T> = Heap(KBuffer.wrap(bytes.getBytesHere()))
    }

    override fun hashCode(): Int = bytes.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Key<*>) return false
        return bytes == other.bytes
    }
}

inline class TransientKey<out T : Any>(val transientBytes: ReadBuffer) {
    fun copyToHeap() = Key.Heap<T>(KBuffer.wrap(transientBytes.getBytes(0)))
}

inline class TransientBytes(val bytes: ReadBuffer) {
    fun copyToHeap() = KBuffer.wrap(bytes.getBytes(0))
}
