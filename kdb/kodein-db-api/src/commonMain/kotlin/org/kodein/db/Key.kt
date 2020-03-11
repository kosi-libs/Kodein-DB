package org.kodein.db

import kotlinx.serialization.*
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.markBuffer
import org.kodein.memory.io.wrap
import org.kodein.memory.text.Base64

@Suppress("unused")
@Serializable(with = Key.KeySerializer::class)
data class Key<out T : Any>(val bytes: ReadMemory) {

    class KeySerializer<T: Any>(@Suppress("UNUSED_PARAMETER") tSerializer: KSerializer<T>) : KSerializer<Key<T>> {
        override val descriptor: SerialDescriptor = PrimitiveDescriptor("org.kodein.db.Key", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Key<T> = Key(KBuffer.wrap(b64Decoder.decode(decoder.decodeString())))

        override fun serialize(encoder: Encoder, value: Key<T>) = encoder.encodeString(value.toBase64())
    }

    fun toBase64(): String = bytes.markBuffer { b64Encoder.encode(it) }

    companion object {
        val b64Encoder = Base64.encoder.withoutPadding()
        val b64Decoder = Base64.decoder
    }

}
