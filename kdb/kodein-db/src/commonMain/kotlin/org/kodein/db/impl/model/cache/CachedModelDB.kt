package org.kodein.db.impl.model.cache

import org.kodein.db.*
import org.kodein.db.data.DataDB
import org.kodein.db.model.*
import org.kodein.db.model.cache.ModelCache
import org.kodein.memory.Closeable
import kotlin.reflect.KClass

internal class CachedModelDB(override val mdb: ModelDB, override val cache: ModelCache, override val copyMaxSize: Long) : CachedModelReadModule, ModelDB, ModelTypeMatcher by mdb, KeyMaker by mdb, ValueMaker by mdb, Closeable by mdb {

    internal fun didPut(model: Any, key: Key<*>, size: Int, options: Array<out Options>) {
        if (ModelCache.Skip in options) cache.evict(key)
        else cache.put(key, model, size)
    }

    internal fun didDelete(key: Key<*>, options: Array<out Options>) {
        if (ModelCache.Skip in options) cache.evict(key)
        else cache.delete(key)
    }

    override fun <M : Any> put(model: M, vararg options: Options.DirectPut): KeyAndSize<M> {
        val key = mdb.keyFrom(model, *options.filterIsInstance<Options.Puts>().toTypedArray())
        val size = mdb.put(key, model, *(options + ReactInLock { didPut(model, key, it, options) }))
        return KeyAndSize(key, size)
    }

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.DirectPut): Int {
        return mdb.put(key, model, *(options + ReactInLock { didPut(model, key, it, options) }))
    }

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.DirectDelete) {
        return mdb.delete(type, key, *(options + ReactInLock { didDelete(key, options) }))
    }

    override fun register(listener: ModelDBListener<Any>): Closeable = mdb.register(listener)

    override fun newBatch(vararg options: Options.NewBatch): ModelBatch = CachedModelBatch(this, mdb.newBatch(*options))

    override fun newSnapshot(vararg options: Options.NewSnapshot): ModelSnapshot {
        val maxSize = maxSize(options)
        return CachedModelSnapshot(mdb.newSnapshot(*options), cache.newCopy(maxSize), maxSize)
    }

    override fun <T : Any> getExtension(key: ExtensionKey<T>): T? = mdb.getExtension(key)

    override val data: DataDB get() = mdb.data
}
