package org.kodein.db

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.markBuffer
import org.kodein.memory.io.wrap
import org.kodein.memory.text.Base64

@Suppress("unused")
@Serializable(with = Key.KeySerializer::class)
public data class Key<out T : Any>(val bytes: ReadMemory) {

    public class KeySerializer<T: Any>(@Suppress("UNUSED_PARAMETER") tSerializer: KSerializer<T>) : KSerializer<Key<T>> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("org.kodein.db.Key", PrimitiveKind.STRING)

        public override fun deserialize(decoder: Decoder): Key<T> = Key(KBuffer.wrap(b64Decoder.decode(decoder.decodeString())))

        public override fun serialize(encoder: Encoder, value: Key<T>): Unit = encoder.encodeString(value.toBase64())
    }

    public fun toBase64(): String = bytes.markBuffer { b64Encoder.encode(it) }

    public companion object {
        public val b64Encoder: Base64.Encoder = Base64.encoder.withoutPadding()
        public val b64Decoder: Base64.Decoder = Base64.decoder
    }

}
