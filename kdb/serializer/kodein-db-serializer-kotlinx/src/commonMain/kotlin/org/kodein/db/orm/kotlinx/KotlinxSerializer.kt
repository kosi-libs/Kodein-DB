package org.kodein.db.orm.kotlinx

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.serializersModuleOf
import org.kodein.db.Options
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.simpleTypeNameOf
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.Writeable
import org.kodein.memory.io.readBytes
import org.kodein.memory.util.UUID
import kotlin.collections.set
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

public object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeLong(value.mostSignificantBits)
        encoder.encodeLong(value.leastSignificantBits)
    }

    override fun deserialize(decoder: Decoder): UUID {
        val mostSignificantBits = decoder.decodeLong()
        val leastSignificantBits = decoder.decodeLong()
        return UUID(mostSignificantBits, leastSignificantBits)
    }
}

public class KotlinxSerializer @JvmOverloads constructor(block: Builder.() -> Unit = {}) : DefaultSerializer {
    private val serializers = HashMap<KType, KSerializer<*>>()

    @OptIn(ExperimentalSerializationApi::class)
    private val cbor = Cbor {
        serializersModule = serializersModuleOf(UUIDSerializer)
    }

    public inner class Builder {
        public fun <T : Any> register(type: KType, serializer: KSerializer<T>) { serializers[type] = serializer }

        @OptIn(ExperimentalStdlibApi::class)
        public inline operator fun <reified T : Any> KSerializer<T>.unaryPlus() {
            register(typeOf<T>(), this)
        }
    }

    init {
        Builder().block()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(type: KType, model: Any, output: Writeable, vararg options: Options.Write) {
        val serializer = serializers[type] ?: serializer(type).also { serializers[type] = it }
        @Suppress("UNCHECKED_CAST")
        val bytes = cbor.encodeToByteArray(serializer as SerializationStrategy<Any>, model)
        output.putBytes(bytes)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(type: KType, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): Any {
        val serializer = serializers[type] ?: serializer(type).also { serializers[type] = it }
        val bytes = input.readBytes()
        @Suppress("UNCHECKED_CAST")
        return cbor.decodeFromByteArray(serializer as DeserializationStrategy<Any>, bytes)
    }

    // TODO: Monitor these issues:
//  - https://github.com/Kotlin/kotlinx.serialization/issues/259
//  - https://github.com/Kotlin/kotlinx.serialization/issues/52

}
