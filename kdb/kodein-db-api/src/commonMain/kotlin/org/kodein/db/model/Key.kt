@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db.model

import org.kodein.memory.*
import org.kodein.memory.text.Base64
import kotlin.reflect.KClass


sealed class Key<out T : Any>(val type: KClass<out T>, val bytes: ReadBuffer) {
    abstract fun asHeapKey(): Key<T>
    class Heap<out T : Any>(type: KClass<out T>, bytes: ReadBuffer) : Key<T>(type, bytes) {
        override fun asHeapKey(): Key<T> = this
    }
    class Native<out T : Any>(type: KClass<out T>, private val alloc: Allocation) : Key<T>(type, alloc), Closeable {
        override fun close() { alloc.close() }
        override fun asHeapKey(): Key<T> = Heap(type, KBuffer.wrap(bytes.getBytesHere()))
    }
}

inline class TransientKey<out T : Any>(val transientKey: Key<T>) {
    fun asPermanent() = Key.Heap(transientKey.type, KBuffer.wrap(transientKey.bytes.getBytes(0)))
}

inline class SeekKey(val bytes: ReadBuffer)

inline class TransientSeekKey(val transientBytes: ReadBuffer) {
    fun asPermanent() = SeekKey(KBuffer.wrap(transientBytes.getBytes(0)))
}
