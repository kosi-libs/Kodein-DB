package org.kodein.db

import kotlinx.serialization.*
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.wrap
import org.kodein.memory.text.Base64

@Suppress("unused")
@Serializable(with = Key.KeySerializer::class)
data class Key<out T : Any>(val bytes: ReadMemory) {

    @Serializer(forClass = Key::class)
    object KeySerializer : KSerializer<Key<*>> {
        private val b64Encoder = Base64.encoder.withoutPadding()
        private val b64Decoder = Base64.decoder

        override val descriptor: SerialDescriptor = PrimitiveDescriptor("org.kodein.db.Key", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Key<*> {
            val b64 = decoder.decodeString()
            val bytes = KBuffer.wrap(b64Decoder.decode(b64))
            return Key<Any>(bytes)
        }

        override fun serialize(encoder: Encoder, value: Key<*>) {
            val b64 = b64Encoder.encode(value.bytes.duplicate())
            encoder.encodeString(b64)
        }
    }


}
