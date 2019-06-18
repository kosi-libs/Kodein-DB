package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.model.Key
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.TransientSeekKey
import org.kodein.memory.ReadBuffer
import org.kodein.memory.model.ObjectCache
import org.kodein.memory.model.Sized

class CachedModelCursor<M : Any>(val cursor: ModelCursor<M>, val cache: ModelCache) : ModelCursor<M> {

    private var cachedEntry: ObjectCache.Entry.Cached<M>? = null

    override fun isValid() = cursor.isValid()

    override fun next() {
        cachedEntry = null
        cursor.next()
    }

    override fun prev() {
        cachedEntry = null
        cursor.prev()
    }

    private inner class Entries(val entries: ModelCursor.Entries<M>) : ModelCursor.Entries<M> {
        override val size: Int get() = entries.size
        override fun getSeekKey(i: Int) = entries.getSeekKey(i)
        override fun getKey(i: Int) = entries.getKey(i)
        override fun close() = entries.close()

        private val cachedEntries = arrayOfNulls<ObjectCache.Entry.Cached<M>>(size)

        override fun getModel(i: Int, vararg options: Options.Read): Sized<M> {
            if (cachedEntries[i] == null) {
                @Suppress("UNCHECKED_CAST")
                cachedEntries[i] = cache.getOrRetrieveEntry(transientKey().asPermanent().asHeapKey()) { entries.getModel(i, *options) } as ObjectCache.Entry.Cached<M>
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
            cachedEntry = cache.getOrRetrieveEntry(transientKey().asPermanent().asHeapKey()) { cursor.model(*options) } as ObjectCache.Entry.Cached<M>
        }
        return cachedEntry!!
    }

    override fun transientSeekKey(): TransientSeekKey = cursor.transientSeekKey()

    override fun close() = cursor.close()

}
