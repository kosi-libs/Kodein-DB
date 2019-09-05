package org.kodein.db.impl.model.cache

import org.kodein.db.*
import org.kodein.db.model.*
import org.kodein.db.model.cache.ModelCache
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.Closeable

internal class CachedModelDB(override val mdb: ModelDB, override val cache: ModelCache, override val copyMaxSize: Long) : CachedModelReadBase, ModelDB, ModelWrite by mdb, Closeable by mdb {

    // https://youtrack.jetbrains.com/issue/KT-20996
    private val listener: DBListener<Any> = object : DBListener<Any> {
        override fun didPut(model: Any, getKey: () -> Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) {
            if (ModelCache.Skip in options) {
                cache.evict(getKey())
            } else {
                cache.put(getKey().asHeapKey(), model, size)
            }
        }

        override fun didDelete(key: Key<*>, model: Any?, typeName: String, options: Array<out Options.Write>) {
            if (ModelCache.Skip in options) {
                cache.evict(key)
            } else {
                cache.delete(key)
            }
        }
    }

    init {
        mdb.register(listener)
    }

    override fun register(listener: DBListener<Any>): Closeable = mdb.register(listener)

    override fun newBatch(): ModelBatch = mdb.newBatch()

    override fun newSnapshot(vararg options: Options.Read): ModelSnapshot {
        val maxSize = maxSize(options)
        return CachedModelSnapshot(mdb.newSnapshot(*options), cache.newCopy(maxSize), maxSize)
    }
}
