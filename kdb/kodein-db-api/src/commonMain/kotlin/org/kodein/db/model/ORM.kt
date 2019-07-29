package org.kodein.db.model

import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.Readable
import org.kodein.memory.io.Writeable
import kotlin.reflect.KClass

interface Serializer<M : Any> {
    fun serialize(model: M, output: Writeable, vararg options: Options.Write)
    fun <R : M> deserialize(type: KClass<R>, input: ReadBuffer, vararg options: Options.Read): R

    class ByClass(val default: Serializer<Any>? = null, build: ByClass.Builder.() -> Unit) : Serializer<Any> {

        val map = HashMap<KClass<*>, Serializer<*>>()

        class Builder(@PublishedApi internal val map: MutableMap<KClass<*>, Serializer<*>>) {
            inline operator fun <reified M : Any> Serializer<M>.unaryPlus() { map[M::class] =  this }
        }

        init { Builder(map).build() }

        @Suppress("UNCHECKED_CAST")
        override fun serialize(model: Any, output: Writeable, vararg options: Options.Write) =
                (map[model::class] as? Serializer<Any>)?.serialize(model, output, *options)
                        ?: default?.serialize(model, output, *options)
                        ?: throw IllegalArgumentException("No serializer found for type ${model::class}")

        @Suppress("UNCHECKED_CAST")
        override fun <M : Any> deserialize(type: KClass<M>, input: ReadBuffer, vararg options: Options.Read): M =
                (map[type] as? Serializer<Any>)?.deserialize(type, input, *options)
                        ?: default?.deserialize(type, input, *options)
                        ?: throw IllegalArgumentException("No serializer found for type $type")

    }

    companion object {
        operator fun <M : Any> invoke(serialize: Writeable.(M) -> Unit, deserialize: ReadBuffer.(KClass<out M>) -> M) = object : Serializer<M> {
            override fun serialize(model: M, output: Writeable, vararg options: Options.Write) = output.serialize(model)

            @Suppress("UNCHECKED_CAST")
            override fun <R : M> deserialize(type: KClass<R>, input: ReadBuffer, vararg options: Options.Read): R = input.deserialize(type) as R
        }
    }
}

interface Metadata : HasMetadata {
    val primaryKey: Value
    val indexes: Set<Index> get() = emptySet()

    override fun getMetadata(db: ModelDB, vararg options: Options.Write) = this

    private class Impl(override val primaryKey: Value, override val indexes: Set<Index> = emptySet()) : Metadata

    companion object {
        operator fun invoke(primaryKey: Value, indexes: Set<Index> = emptySet()): Metadata = Impl(primaryKey, indexes)
    }
}

interface MetadataExtractor {
    fun extractMetadata(model: Any, vararg options: Options.Write): Metadata
}

class NoMetadataExtractor : MetadataExtractor {
    override fun extractMetadata(model: Any, vararg options: Options.Write): Metadata =
            throw IllegalStateException("No Metadata extractor defined: models must implement HasMetadata")

}

interface HasMetadata {
    fun getMetadata(db: ModelDB, vararg options: Options.Write): Metadata
}
