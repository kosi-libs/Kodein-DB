package org.kodein.db.model.orm

import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.indexSet
import org.kodein.db.model.ModelDB

public interface Metadata : HasMetadata {
    public val id: Any
    public fun indexes(): Set<Index> = emptySet()

    override fun getMetadata(db: ModelDB, vararg options: Options.Write): Metadata = this

    private class Impl(override val id: Any, val indexes: Set<Index> = emptySet()) : Metadata {
        override fun indexes(): Set<Index> = indexes
    }

    public companion object {
        public operator fun invoke(id: Any, indexes: Set<Index>): Metadata = Impl(id, indexes)
        public operator fun invoke(id: Any, vararg indexes: Pair<String, Any>): Metadata = Impl(id, indexSet(*indexes))
    }
}

public fun interface MetadataExtractor : Options.Open {
    public fun extractMetadata(model: Any, vararg options: Options.Write): Metadata?

    public companion object {
        public inline fun <reified M: Any> forClass(crossinline extractor: (model: M, options: Array<out Options.Write>) -> Metadata): MetadataExtractor =
                MetadataExtractor { model, options ->
                    if (model is M) extractor(model, options)
                    else null
                }
    }
}

public fun interface HasMetadata {
    public fun getMetadata(db: ModelDB, vararg options: Options.Write): Metadata
}
