@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db.orm.kryo

import org.kodein.db.Options
import org.kodein.db.model.*
import org.kodein.memory.KBuffer
import org.kodein.memory.backingArray
import org.kodein.memory.getBytes
import org.kodein.memory.text.Base64
import org.kodein.memory.wrap


inline class Ref<out T : Any>(val bytes: ByteArray)

inline class SeekRef(val bytes: ByteArray)


fun <T : Any> Key<T>.asRef() = Ref<T>(bytes.getBytes(0))

fun <T : Any> TransientKey<T>.asRef() = Ref<T>(transientKey.bytes.getBytes(0))

fun SeekKey.asRef() = SeekRef(bytes.getBytes(0))

fun TransientSeekKey.asRef() = SeekRef(transientBytes.getBytes(0))


inline fun <reified T : Any> Ref<T>.toKey() = Key(T::class, KBuffer.wrap(bytes))

fun SeekRef.toSeekKey() = SeekKey(KBuffer.wrap(bytes))


inline operator fun <reified M : Any> ModelRead.get(ref: Ref<M>, vararg options: Options.Read) = get(ref.toKey(), *options)
