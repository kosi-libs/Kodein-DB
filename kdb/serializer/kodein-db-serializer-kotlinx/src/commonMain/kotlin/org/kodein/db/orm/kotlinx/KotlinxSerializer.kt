package org.kodein.db.orm.kotlinx

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.internal.HexConverter
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.invoke
import org.kodein.db.model.orm.Serializer
import org.kodein.db.simpleNameOf
import org.kodein.memory.io.*
import org.kodein.memory.text.Base64
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass

class KXSerializer(val serializer: KSerializer<*>) : Options.Read, Options.Write

class KotlinxSerializer @JvmOverloads constructor(block: Builder.() -> Unit = {}) : Serializer<Any> {
    private val serializers = HashMap<KClass<*>, KSerializer<*>>()

    private object KeySerializer : KSerializer<Key<*>> {
        private val b64Encoder = Base64.encoder.withoutPadding()
        private val b64Decoder = Base64.decoder

        override val descriptor: SerialDescriptor = StringDescriptor.withName("DbKey")

        override fun deserialize(decoder: Decoder): Key<*> {
            val b64 = decoder.decodeString()
            val bytes = KBuffer.wrap(b64Decoder.decode(b64))
            return Key.Heap<Any>(bytes)
        }

        override fun serialize(encoder: Encoder, obj: Key<*>) {
            val b64 = b64Encoder.encode(obj.bytes.duplicate())
            encoder.encodeString(b64)
        }

        @Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
        inline fun <T : Key<*>> out() = this as KSerializer<T>
    }

    private val cbor = Cbor(context = SerializersModule {
        contextual(KeySerializer)
        contextual(KeySerializer.out<Key.Heap<*>>())
        contextual(KeySerializer.out<Key.Native<*>>())

        polymorphic<Key<*>> {
            Key.Heap::class with (KeySerializer.out())
            Key.Native::class with (KeySerializer.out())
        }
//        polymorphic<org.kodein.db.Ref<*>> {
//            Ref::class with Ref.serializer()
//        }
    })

    inner class Builder {
        fun <T : Any> register(type: KClass<T>, serializer: KSerializer<T>) { serializers[type] = serializer }

        inline operator fun <reified T : Any> KSerializer<T>.unaryPlus() {
            register(T::class, this)
        }
    }

    init {
        Builder().block()
    }

    fun <T : Any> register(type: KClass<T>, serializer: KSerializer<T>) { serializers[type] = serializer }

    @ImplicitReflectionSerializer
    private fun getSerializer(options: Array<out Options>, type: KClass<*>): KSerializer<*> {
        return try {
            options<KXSerializer>()?.serializer ?: serializers[type] ?: type.serializer()
        } catch (ex: NotImplementedError) {
            throw IllegalStateException("Could not find serializer for class ${simpleNameOf(type)}. Hove you registered the serializer?", ex)
        }
    }

    @ImplicitReflectionSerializer
    override fun serialize(model: Any, output: Writeable, vararg options: Options.Write) {
        @Suppress("UNCHECKED_CAST")
        val bytes = cbor.dump(getSerializer(options, model::class) as SerializationStrategy<Any>, model)
        output.putBytes(bytes)
    }

    @ImplicitReflectionSerializer
    override fun <M : Any> deserialize(type: KClass<M>, input: ReadBuffer, vararg options: Options.Read): M {
        val serializer = options<KXSerializer>()?.serializer ?: serializers[type] ?: type.serializer()
        val bytes = input.readBytes()
        @Suppress("UNCHECKED_CAST")
        return cbor.load(serializer as DeserializationStrategy<M>, bytes)
    }

    // TODO: Monitor these issues:
//  - https://github.com/Kotlin/kotlinx.serialization/issues/259
//  - https://github.com/Kotlin/kotlinx.serialization/issues/52

}
