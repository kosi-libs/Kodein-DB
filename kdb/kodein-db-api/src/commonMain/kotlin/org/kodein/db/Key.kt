package org.kodein.db

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.kodein.memory.io.*
import org.kodein.memory.text.Base64
import org.kodein.memory.text.toHex

@Suppress("unused")
@Serializable(with = Key.KeySerializer::class)
public data class Key<out T : Any>(val bytes: ReadMemory) {

    public class KeySerializer<T: Any>(@Suppress("UNUSED_PARAMETER") tSerializer: KSerializer<T>) : KSerializer<Key<T>> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("org.kodein.db.Key", PrimitiveKind.STRING)

        public override fun deserialize(decoder: Decoder): Key<T> = Key(Memory.wrap(b64Decoder.decode(decoder.decodeString())))

        public override fun serialize(encoder: Encoder, value: Key<T>): Unit = encoder.encodeString(value.toBase64())
    }

    public fun toBase64(): String = b64Encoder.encode(bytes.asReadable())

    override fun toString(): String = "Key(${bytes.getInt(2)}: ${bytes.slice(6, bytes.size - 7).toHex()})"

    public companion object {
        public val b64Encoder: Base64.Encoder = Base64.encoder.withoutPadding()
        public val b64Decoder: Base64.Decoder = Base64.decoder
    }

}
