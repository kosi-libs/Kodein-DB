package org.kodein.db.model.orm

import org.kodein.db.Options
import org.kodein.db.model.ModelDB

public interface Metadata : HasMetadata {
    public val id: Any
    public fun indexes(): Map<String, Any> = emptyMap()

    override fun getMetadata(db: ModelDB, vararg options: Options.Write): Metadata = this

    private class Impl(override val id: Any, val indexes: Map<String, Any> = emptyMap()) : Metadata {
        override fun indexes(): Map<String, Any> = indexes
    }

    public companion object {
        public operator fun invoke(id: Any, indexes: Map<String, Any>): Metadata = Impl(id, indexes)
        public operator fun invoke(id: Any, vararg indexes: Pair<String, Any>): Metadata = Impl(id, mapOf(*indexes))
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
