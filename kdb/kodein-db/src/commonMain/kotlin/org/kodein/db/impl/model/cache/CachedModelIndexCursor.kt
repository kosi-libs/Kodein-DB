package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.model.ModelIndexCursor
import org.kodein.db.model.cache.ModelCache
import kotlin.reflect.KClass


internal class CachedModelIndexCursor<M : Any>(override val cursor: ModelIndexCursor<M>, cache: ModelCache) : CachedModelCursor<M>(cursor, cache), ModelIndexCursor<M> {
    override fun <T : Any> associatedObject(type: KClass<T>, vararg options: Options.Get): T? = cursor.associatedObject(type, *options)
}
