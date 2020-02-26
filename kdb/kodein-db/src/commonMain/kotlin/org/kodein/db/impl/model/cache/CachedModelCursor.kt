package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.impl.model.ResettableCursorModule
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.cache.ModelCache
import org.kodein.memory.Closeable

internal class CachedModelCursor<M : Any>(override val cursor: ModelCursor<M>, val cache: ModelCache) : ModelCursor<M>, ResettableCursorModule, Closeable by cursor {

    private var cachedEntry: ModelCache.Entry.Cached<M>? = null

    override fun reset() {
        cachedEntry = null
    }

    override fun key() = cursor.key()

    override fun model(vararg options: Options.Read): Sized<M> {
        if (cachedEntry == null) {
            @Suppress("UNCHECKED_CAST")
            cachedEntry = cache.getOrRetrieveEntry(key()) { cursor.model(*options) } as ModelCache.Entry.Cached<M>
        }
        return cachedEntry!!
    }

    override fun duplicate(): ModelCursor<M> = CachedModelCursor(cursor.duplicate(), cache)
}
