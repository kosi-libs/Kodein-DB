package org.kodein.db.orm.kotlinx

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import org.kodein.db.Options
import org.kodein.db.Ref
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

    private val encoder = Base64.encoder.withoutPadding()
    private val decoder = Base64.decoder

    private val cbor = Cbor(context = SerializersModule {
        polymorphic<org.kodein.db.Ref<*>> {
            Ref::class with Ref.serializer()
        }
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

    @Serializable
/*inline*/ data class Ref(val b64: String) : org.kodein.db.Ref<Any>

    @Suppress("UNCHECKED_CAST")
    override fun <M : Any> getRef(bytes: ReadBuffer) = Ref(encoder.encode(bytes.duplicate())) as org.kodein.db.Ref<M>

    override fun getBytes(ref: org.kodein.db.Ref<*>) = KBuffer.wrap(decoder.decode((ref as Ref).b64))
}
