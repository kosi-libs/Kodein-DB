package org.kodein.db.orm.kotlinx

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.cbor.CborBuilder
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.serializersModuleOf
import org.kodein.db.Options
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.db.simpleTypeNameOf
import org.kodein.memory.io.CursorReadable
import org.kodein.memory.io.Writeable
import org.kodein.memory.io.readBytes
import org.kodein.memory.io.writeBytes
import org.kodein.memory.util.UUID
import kotlin.collections.set
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass

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

public class KotlinxSerializer @JvmOverloads constructor(module: SerializersModule? = null, block: Builder.() -> Unit = {}) : DefaultSerializer {
    private val serializers = HashMap<KClass<*>, KSerializer<*>>()

    private val builder = Builder().apply(block)

    @ExperimentalSerializationApi
    private val cbor = Cbor {
        builder.cborConfigurations.forEach { it(this) }

        serializersModule = SerializersModule {
            include(serializersModuleOf(UUIDSerializer))
            if (module != null) include(module)
        }
    }

    public inner class Builder {
        public fun <T : Any> register(type: KClass<T>, serializer: KSerializer<T>) { serializers[type] = serializer }

        public inline operator fun <reified T : Any> KSerializer<T>.unaryPlus() {
            register(T::class, this)
        }

        @ExperimentalSerializationApi
        internal val cborConfigurations = ArrayList<CborBuilder.() -> Unit>()

        @ExperimentalSerializationApi
        public fun configure(block: CborBuilder.() -> Unit) {
            cborConfigurations += block
        }
    }

    @InternalSerializationApi
    private fun getSerializer(type: KClass<*>): KSerializer<*> {
        return try {
            serializers[type] ?: type.serializer()
        } catch (ex: NotImplementedError) {
            throw IllegalStateException("Could not find serializer for class ${simpleTypeNameOf(type)}. Hove you registered the serializer?", ex)
        }
    }

    @InternalSerializationApi @ExperimentalSerializationApi
    override fun serialize(model: Any, output: Writeable, vararg options: Options.Puts) {
        @Suppress("UNCHECKED_CAST")
        val bytes = cbor.encodeToByteArray(getSerializer(model::class) as SerializationStrategy<Any>, model)
        output.writeBytes(bytes)
    }

    @InternalSerializationApi @ExperimentalSerializationApi
    override fun deserialize(type: KClass<out Any>, input: CursorReadable, vararg options: Options.Get): Any {
        val serializer = serializers[type] ?: type.serializer().also { serializers[type] = it }
        val bytes = input.readBytes()
        @Suppress("UNCHECKED_CAST")
        return cbor.decodeFromByteArray(serializer as DeserializationStrategy<Any>, bytes)
    }

    // TODO: Monitor these issues:
//  - https://github.com/Kotlin/kotlinx.serialization/issues/259
//  - https://github.com/Kotlin/kotlinx.serialization/issues/52

}
