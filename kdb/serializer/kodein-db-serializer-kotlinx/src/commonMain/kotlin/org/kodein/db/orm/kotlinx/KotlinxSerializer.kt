package org.kodein.db.orm.kotlinx

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import org.kodein.db.Options
import org.kodein.db.invoke
import org.kodein.db.model.orm.Serializer
import org.kodein.db.simpleTypeNameOf
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.Writeable
import org.kodein.memory.io.readBytes
import kotlin.collections.HashMap
import kotlin.collections.set
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass

class KXSerializer(val serializer: KSerializer<*>) : Options.Read, Options.Write

class KotlinxSerializer @JvmOverloads constructor(block: Builder.() -> Unit = {}) : Serializer<Any> {
    private val serializers = HashMap<KClass<*>, KSerializer<*>>()

    private val cbor = Cbor()

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
    private fun getSerializer(options: Array<out Options>, type: KClass<*>): KSerializer<*> {
        return try {
            options<KXSerializer>()?.serializer ?: serializers[type] ?: type.serializer()
        } catch (ex: NotImplementedError) {
            throw IllegalStateException("Could not find serializer for class ${simpleTypeNameOf(type)}. Hove you registered the serializer?", ex)
        }
    }

    @ImplicitReflectionSerializer
    override fun serialize(model: Any, output: Writeable, vararg options: Options.Write) {
        @Suppress("UNCHECKED_CAST")
        val bytes = cbor.dump(getSerializer(options, model::class) as SerializationStrategy<Any>, model)
        output.putBytes(bytes)
    }

    @ImplicitReflectionSerializer
    override fun deserialize(type: KClass<out Any>, transientId: ReadBuffer, input: ReadBuffer, vararg options: Options.Read): Any {
        val serializer = options<KXSerializer>()?.serializer ?: serializers[type] ?: type.serializer().also { serializers[type] = it }
        val bytes = input.readBytes()
        @Suppress("UNCHECKED_CAST")
        return cbor.load(serializer as DeserializationStrategy<Any>, bytes)
    }

    // TODO: Monitor these issues:
//  - https://github.com/Kotlin/kotlinx.serialization/issues/259
//  - https://github.com/Kotlin/kotlinx.serialization/issues/52

}
