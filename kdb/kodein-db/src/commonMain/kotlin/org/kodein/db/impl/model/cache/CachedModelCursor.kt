package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.cache.ModelCache
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadBuffer

internal class CachedModelCursor<M : Any>(val cursor: ModelCursor<M>, val cache: ModelCache) : ModelCursor<M>, Closeable by cursor {

    private var cachedEntry: ModelCache.Entry.Cached<M>? = null

    override fun isValid() = cursor.isValid()

    override fun next() {
        cachedEntry = null
        cursor.next()
    }

    override fun prev() {
        cachedEntry = null
        cursor.prev()
    }

    private inner class Entries(val entries: ModelCursor.Entries<M>) : ModelCursor.Entries<M> by entries {
        private val cachedEntries = arrayOfNulls<ModelCache.Entry.Cached<M>>(size)

        override fun get(i: Int, vararg options: Options.Read): Sized<M> {
            if (cachedEntries[i] == null) {
                @Suppress("UNCHECKED_CAST")
                cachedEntries[i] = cache.getOrRetrieveEntry(transientKey().asHeapKey()) { entries.get(i, *options) } as ModelCache.Entry.Cached<M>
            }
            return cachedEntries[i]!!
        }
    }

    override fun nextEntries(size: Int): ModelCursor.Entries<M> = Entries(cursor.nextEntries(size))

    override fun seekToFirst() {
        cachedEntry = null
        cursor.seekToFirst()
    }

    override fun seekToLast() {
        cachedEntry = null
        cursor.seekToLast()
    }

    override fun seekTo(target: ReadBuffer) {
        cachedEntry = null
        cursor.seekTo(target)
    }

    override fun transientKey() = cursor.transientKey()

    override fun model(vararg options: Options.Read): Sized<M> {
        if (cachedEntry == null) {
            @Suppress("UNCHECKED_CAST")
            cachedEntry = cache.getOrRetrieveEntry(transientKey().asHeapKey()) { cursor.model(*options) } as ModelCache.Entry.Cached<M>
        }
        return cachedEntry!!
    }

    override fun transientSeekKey() = cursor.transientSeekKey()

}
