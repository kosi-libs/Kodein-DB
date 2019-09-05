package org.kodein.db.model.orm

import org.kodein.db.Options
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.Writeable
import kotlin.reflect.KClass

interface Serializer<M : Any> : RefMapper {
    fun serialize(model: M, output: Writeable, vararg options: Options.Write)
    fun <R : M> deserialize(type: KClass<R>, input: ReadBuffer, vararg options: Options.Read): R

    sealed class Default {
        internal abstract val default: org.kodein.db.model.orm.RefMapper
        class Serializer(override val default: org.kodein.db.model.orm.Serializer<Any>) : Default()
        class RefMapper(override val default: org.kodein.db.model.orm.RefMapper) : Default()
    }

    class ByClass(val default: Default = Default.RefMapper(BytesRefMapper.instance), build: Builder.() -> Unit) : Serializer<Any>, RefMapper by default.default {

        val map = HashMap<KClass<*>, Serializer<*>>()

        class Builder(@PublishedApi internal val map: MutableMap<KClass<*>, Serializer<*>>) {
            inline operator fun <reified M : Any> Serializer<M>.unaryPlus() { map[M::class] =  this }
        }

        init { Builder(map).build() }

        @Suppress("UNCHECKED_CAST")
        override fun serialize(model: Any, output: Writeable, vararg options: Options.Write) =
                (map[model::class] as? Serializer<Any>)?.serialize(model, output, *options)
                        ?: (default as? Default.Serializer)?.default?.serialize(model, output, *options)
                        ?: throw IllegalArgumentException("No serializer found for type ${model::class}")

        @Suppress("UNCHECKED_CAST")
        override fun <M : Any> deserialize(type: KClass<M>, input: ReadBuffer, vararg options: Options.Read): M =
                (map[type] as? Serializer<Any>)?.deserialize(type, input, *options)
                        ?: (default as? Default.Serializer)?.default?.deserialize(type, input, *options)
                        ?: throw IllegalArgumentException("No serializer found for type $type")
    }
}