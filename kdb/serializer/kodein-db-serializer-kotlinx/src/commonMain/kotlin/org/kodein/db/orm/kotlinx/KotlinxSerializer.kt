package org.kodein.db.orm.kotlinx

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.serializersModule
import org.kodein.db.Options
import org.kodein.db.invoke
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

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveDescriptor("UUID", PrimitiveKind.STRING)

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

class KotlinxSerializer @JvmOverloads constructor(block: Builder.() -> Unit = {}) : DefaultSerializer {
    private val serializers = HashMap<KClass<*>, KSerializer<*>>()

    private val cbor = Cbor(updateMode = UpdateMode.UPDATE, context = serializersModule(UUIDSerializer))

    inner class Builder {
        fun <T : Any> register(type: KClass<T>, serializer: KSerializer<T>) { serializers[type] = serializer }

        inline operator fun <reified T : Any> KSerializer<T>.unaryPlus() {
            register(T::class, this)
        }
    }

    init {
        Builder().block()
    }

    @ImplicitReflectionSerializer
    private fun getSerializer(type: KClass<*>): KSerializer<*> {
        return try {
            serializers[type] ?: type.serializer()
        } catch (ex: NotImplementedError) {
            throw IllegalStateException("Could not find serializer for class ${simpleTypeNameOf(type)}. Hove you registered the serializer?", ex)
        }
    }

    @ImplicitReflectionSerializer
    override fun serialize(model: Any, output: Writeable, vararg options: Options.Write) {
        @Suppress("UNCHECKED_CAST")
        val bytes = cbor.dump(getSerializer(model::class) as SerializationStrategy<Any>, model)
        output.putBytes(bytes)
    }

    @ImplicitReflectionSerializer
    override fun deserialize(type: KClass<out Any>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): Any {
        val serializer = serializers[type] ?: type.serializer().also { serializers[type] = it }
        val bytes = input.readBytes()
        @Suppress("UNCHECKED_CAST")
        return cbor.load(serializer as DeserializationStrategy<Any>, bytes)
    }

    // TODO: Monitor these issues:
//  - https://github.com/Kotlin/kotlinx.serialization/issues/259
//  - https://github.com/Kotlin/kotlinx.serialization/issues/52

}
