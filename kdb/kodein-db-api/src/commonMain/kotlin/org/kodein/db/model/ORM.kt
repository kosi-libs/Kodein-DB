package org.kodein.db.model

import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.memory.io.Readable
import org.kodein.memory.io.Writeable
import kotlin.reflect.KClass

interface Serializer {
    fun <M : Any> serialize(model: M, output: Writeable, vararg options: Options.Write)
    fun <M : Any> deserialize(type: KClass<M>, input: Readable, vararg options: Options.Read): M
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
