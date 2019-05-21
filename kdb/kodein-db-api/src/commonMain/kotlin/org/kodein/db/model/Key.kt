@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db.model

import org.kodein.memory.KBuffer
import org.kodein.memory.ReadBuffer
import org.kodein.memory.getBytes
import org.kodein.memory.text.Base64
import org.kodein.memory.wrap
import kotlin.reflect.KClass


class Key<out T : Any>(val type: KClass<out T>, val bytes: ReadBuffer)

inline class TransientKey<out T : Any>(val transientKey: Key<T>) {
    fun asPermanent() = Key(transientKey.type, KBuffer.wrap(transientKey.bytes.getBytes(0)))
}

inline class SeekKey(val bytes: ReadBuffer)

inline class TransientSeekKey(val transientBytes: ReadBuffer) {
    fun asPermanent() = SeekKey(KBuffer.wrap(transientBytes.getBytes(0)))
}
