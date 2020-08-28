package org.kodein.db.impl.model.cache

import org.kodein.db.*
import org.kodein.db.model.ModelBatch
import org.kodein.db.model.ModelDB
import org.kodein.db.model.ModelSnapshot
import org.kodein.db.model.cache.ModelCache
import org.kodein.memory.Closeable
import kotlin.reflect.KClass

internal class CachedModelDB(override val mdb: ModelDB, override val cache: ModelCache, override val copyMaxSize: Long) : CachedModelReadModule, ModelDB, KeyMaker by mdb, Closeable by mdb {

    internal fun didPut(model: Any, key: Key<*>, size: Int, options: Array<out Options>) {
        if (ModelCache.Skip in options) cache.evict(key)
        else cache.put(key, model, size)
    }

    internal fun didDelete(key: Key<*>, options: Array<out Options.Write>) {
        if (ModelCache.Skip in options) cache.evict(key)
        else cache.delete(key)
    }

    override fun <M : Any> put(model: M, vararg options: Options.Write): KeyAndSize<M> {
        val key = mdb.keyFrom(model, *options)
        val size = mdb.put(key, model, *(options + React(true) { didPut(model, key, it, options) }))
        return KeyAndSize(key, size)
    }

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Write): Int {
        return mdb.put(key, model, *(options + React(true) { didPut(model, key, it, options) }))
    }

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write) {
        return mdb.delete(type, key, *(options + React(true) { didDelete(key, options) }))
    }

    override fun register(listener: DBListener<Any>): Closeable = mdb.register(listener)

    override fun newBatch(): ModelBatch = CachedModelBatch(this, mdb.newBatch())

    override fun newSnapshot(vararg options: Options.Read): ModelSnapshot {
        val maxSize = maxSize(options)
        return CachedModelSnapshot(mdb.newSnapshot(*options), cache.newCopy(maxSize), maxSize)
    }
}
