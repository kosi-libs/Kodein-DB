package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.model.Key
import org.kodein.db.model.ModelDB
import org.kodein.db.react.DBListener
import org.kodein.memory.Closeable

class CachedModelDB(override val mdb: ModelDB, override val cache: ModelCache, override val cacheCopyMaxSize: Int) : ModelDB, BaseCachedModelRead, BaseCachedModelWrite {

    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String> = mdb.getIndexesOf(key, *options)

    override fun close() = mdb.close()

    override fun cacheEvict(key: Key<*>) {
        cache.evict(key)
    }

    override fun cacheDelete(key: Key<*>) {
        cache.delete(key)
    }

    override fun cachePut(heapKey: Key<*>, model: Any, size: Int) {
        cache.put(heapKey, model, size)
    }

    override fun newBatch(): ModelDB.Batch = CachedModelBatch(mdb.newBatch(), cache)

    override fun newSnapshot(vararg options: Options.Read): ModelDB.Snapshot {
        val maxSize = maxSize(options)
        return CachedModelSnapshot(mdb.newSnapshot(*options), cache.newCopy(maxSize), maxSize)
    }

    override fun register(listener: DBListener, vararg options: Options.React): Closeable = mdb.register(listener, *options)
}
