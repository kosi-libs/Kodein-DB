package org.kodein.db.orm.kotlinx

import kotlinx.serialization.*
import kotlinx.serialization.cbor.Cbor
import org.kodein.db.Options
import org.kodein.db.invoke
import org.kodein.db.model.Serializer
import org.kodein.db.simpleNameOf
import org.kodein.memory.Readable
import org.kodein.memory.Writeable
import org.kodein.memory.readBytes
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass

class KXSerializer(val serializer: KSerializer<*>) : Options.Read, Options.Write

class KotlinxSerializer @JvmOverloads constructor(block: Builder.() -> Unit = {}) : Serializer {

    private val serializers = HashMap<KClass<*>, KSerializer<*>>()

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
        val opt: KXSerializer? = options()
        return try {
            opt?.serializer ?: serializers[type] ?: type.serializer()
        } catch (ex: NotImplementedError) {
            throw IllegalStateException("Could not find serializer for class ${simpleNameOf(type)}. Hove you registered the serializer?", ex)
        }
    }

    @ImplicitReflectionSerializer
    override fun <M : Any> serialize(model: M, output: Writeable, vararg options: Options.Write) {
        @Suppress("UNCHECKED_CAST")
        val bytes = Cbor.dump(getSerializer(options, model::class) as SerializationStrategy<M>, model)
        output.putBytes(bytes)
    }

    @ImplicitReflectionSerializer
    override fun <M : Any> deserialize(type: KClass<M>, input: Readable, vararg options: Options.Read): M {
        val opt: KXSerializer? = options()
        val serializer = opt?.serializer ?: serializers[type] ?: type.serializer()
        val bytes = input.readBytes()
        @Suppress("UNCHECKED_CAST")
        return Cbor.load(serializer as DeserializationStrategy<M>, bytes)
    }

}
