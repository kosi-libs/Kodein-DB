@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package org.kodein.db

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.wrap
import org.kodein.memory.text.Base64

@Suppress("unused")
@Serializable(with = Key.KxSerializer::class)
class Key<out T : Any>(val bytes: ReadMemory) {
    override fun hashCode(): Int = bytes.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Key<*>) return false
        return bytes == other.bytes
    }

    @Serializer(forClass = Key::class)
    object KxSerializer : KSerializer<Key<*>> {
        private val b64Encoder = Base64.encoder.withoutPadding()
        private val b64Decoder = Base64.decoder

        override val descriptor: SerialDescriptor = StringDescriptor.withName("org.kodein.db.Key")

        override fun deserialize(decoder: Decoder): Key<*> {
            val b64 = decoder.decodeString()
            val bytes = KBuffer.wrap(b64Decoder.decode(b64))
            return Key<Any>(bytes)
        }

        override fun serialize(encoder: Encoder, obj: Key<*>) {
            val b64 = b64Encoder.encode(obj.bytes.duplicate())
            encoder.encodeString(b64)
        }
    }
}
