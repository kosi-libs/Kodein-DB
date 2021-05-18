package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.model.ModelIndexCursor
import org.kodein.db.model.cache.ModelCache
import org.kodein.memory.io.ReadMemory
import kotlin.reflect.KClass


internal class CachedModelIndexCursor<M : Any>(override val cursor: ModelIndexCursor<M>, cache: ModelCache) : CachedModelCursor<M>(cursor, cache), ModelIndexCursor<M> {
    override fun transientAssociatedData(): ReadMemory? = cursor.transientAssociatedData()
}
