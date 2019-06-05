@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db.orm.kotlinx

import kotlinx.serialization.Serializable
import org.kodein.db.Options
import org.kodein.db.model.*
import org.kodein.memory.KBuffer
import org.kodein.memory.getBytes
import org.kodein.memory.getBytesHere
import org.kodein.memory.text.Base64
import org.kodein.memory.wrap

private val encoder = Base64.encoder.withoutPadding()
private val decoder = Base64.decoder


// TODO: Monitor these issues:
//  - https://github.com/Kotlin/kotlinx.serialization/issues/259
//  - https://github.com/Kotlin/kotlinx.serialization/issues/385

@Serializable
/*inline*/ data class Ref<out T : Any>(val b64: String)

@Serializable
/*inline*/ data class SeekRef(val b64: String)


fun <T : Any> Key<T>.asRef() = Ref<T>(encoder.encode(bytes.duplicate()))

fun <T : Any> TransientKey<T>.asRef() = Ref<T>(encoder.encode(transientKey.bytes.duplicate()))

fun SeekKey.asRef() = SeekRef(encoder.encode(bytes.duplicate()))

fun TransientSeekKey.asRef() = SeekRef(encoder.encode(transientBytes.duplicate()))


@PublishedApi
internal fun <T : Any> Ref<T>.decodeBytes() = decoder.decode(b64)

inline fun <reified T : Any> Ref<T>.toKey() = Key.Heap(T::class, KBuffer.wrap(decodeBytes()))

fun SeekRef.toSeekKey() = SeekKey(KBuffer.wrap(decoder.decode(b64)))


inline operator fun <reified M : Any> ModelRead.get(ref: Ref<M>, vararg options: Options.Read) = get(ref.toKey(), *options)
