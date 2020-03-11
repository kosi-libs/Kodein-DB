package org.kodein.db.model.orm

import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.indexSet
import org.kodein.db.model.ModelDB

interface Metadata : HasMetadata {
    val id: Any
    fun indexes(): Set<Index> = emptySet()

    override fun getMetadata(db: ModelDB, vararg options: Options.Write) = this

    private class Impl(override val id: Any, val indexes: Set<Index> = emptySet()) : Metadata {
        override fun indexes(): Set<Index> = indexes
    }

    companion object {
        operator fun invoke(id: Any, indexes: Set<Index>): Metadata = Impl(id, indexes)
        operator fun invoke(id: Any, vararg indexes: Pair<String, Any>): Metadata = Impl(id, indexSet(*indexes))
    }
}

interface MetadataExtractor : Options.Open {
    fun extractMetadata(model: Any, vararg options: Options.Write): Metadata

    companion object {
        operator fun invoke(extractor: (Any) -> Metadata) = object : MetadataExtractor {
            override fun extractMetadata(model: Any, vararg options: Options.Write): Metadata = extractor.invoke(model)
        }
    }
}

class NoMetadataExtractor : MetadataExtractor {
    override fun extractMetadata(model: Any, vararg options: Options.Write): Metadata =
            throw IllegalStateException("No Metadata extractor defined: models must implement HasMetadata")

}

interface HasMetadata {
    fun getMetadata(db: ModelDB, vararg options: Options.Write): Metadata
}
