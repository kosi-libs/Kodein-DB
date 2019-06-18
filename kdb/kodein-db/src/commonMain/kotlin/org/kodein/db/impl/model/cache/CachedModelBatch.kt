package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.model.Cache
import org.kodein.db.model.Key
import org.kodein.db.model.ModelDB
import org.kodein.memory.model.ObjectCacheBase
import org.kodein.memory.model.Sized

class CachedModelBatch(override val mdb: ModelDB.Batch, val cache: ModelCache) : ModelDB.Batch, BaseCachedModelWrite {

    override fun close() = mdb.close()

    private val cacheOperations = ArrayList<ObjectCacheBase<Key<*>, Any>.() -> Unit>()

    override fun cacheEvict(key: Key<*>) {
        val heapKey = key.asHeapKey()
        cacheOperations += { evict(heapKey) }
    }

    override fun cacheDelete(key: Key<*>) {
        val heapKey = key.asHeapKey()
        cacheOperations += { delete(heapKey) }
    }

    override fun cachePut(heapKey: Key<*>, model: Any, size: Int) {
        cacheOperations += { put(heapKey, model, size) }
    }

    override fun write(vararg options: Options.Write) {
        cache.batch {
            mdb.write(*options)
            cacheOperations.forEach { it() }
        }
    }
}
