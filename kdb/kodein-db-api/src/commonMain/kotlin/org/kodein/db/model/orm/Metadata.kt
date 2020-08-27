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

public interface MetadataExtractor : Options.Open {
    public fun extractMetadata(model: Any, vararg options: Options.Write): Metadata

    public companion object {
        public operator fun invoke(extractor: (Any) -> Metadata): MetadataExtractor = object : MetadataExtractor {
            override fun extractMetadata(model: Any, vararg options: Options.Write): Metadata = extractor.invoke(model)
        }
    }
}

public class NoMetadataExtractor : MetadataExtractor {
    override fun extractMetadata(model: Any, vararg options: Options.Write): Metadata =
            throw IllegalStateException("No Metadata extractor defined: models must implement HasMetadata")

}

public interface HasMetadata {
    public fun getMetadata(db: ModelDB, vararg options: Options.Write): Metadata
}
