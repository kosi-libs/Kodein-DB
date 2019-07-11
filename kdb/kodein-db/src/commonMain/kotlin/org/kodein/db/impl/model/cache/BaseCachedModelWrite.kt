package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.model.Cache
import org.kodein.db.model.Key
import org.kodein.db.model.ModelWrite
import org.kodein.memory.cache.Sized

interface BaseCachedModelWrite : ModelWrite, BaseCachedModelBase {

    override val mdb: ModelWrite

    fun cacheEvict(key: Key<*>)

    fun cacheDelete(key: Key<*>)

    fun cachePut(heapKey: Key<*>, model: Any, size: Int)

    private fun putInCache(sized: Sized<Key<*>>, model: Any, options: Array<out Options.Write>) {
        if (Cache.Skip in options) {
            cacheEvict(sized.value)
        } else {
            cachePut(sized.value.asHeapKey(), model, sized.size)
        }
    }

    override fun put(model: Any, vararg options: Options.Write): Int {
        val sized = mdb.putAndGetHeapKey(model, *options)
        putInCache(sized, model, options)
        return sized.size
    }

    override fun <M : Any> putAndGetHeapKey(model: M, vararg options: Options.Write): Sized<Key<M>> {
        val sized = mdb.putAndGetHeapKey(model, *options)
        putInCache(sized, model, options)
        return sized
    }

    override fun <M : Any> putAndGetNativeKey(model: M, vararg options: Options.Write): Sized<Key.Native<M>> {
        val sized = mdb.putAndGetNativeKey(model, *options)
        putInCache(sized, model, options)
        return sized
    }

    override fun delete(key: Key<*>, vararg options: Options.Write) {
        mdb.delete(key, *options)
        if (Cache.Skip in options) {
            cacheEvict(key)
        } else {
            cacheDelete(key.asHeapKey())
        }
    }
}
