package org.kodein.db.impl.model.cache

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.invoke
import org.kodein.db.model.Cache
import org.kodein.db.model.Key
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.ModelRead
import org.kodein.memory.cache.ObjectCache
import org.kodein.memory.cache.Sized
import kotlin.reflect.KClass

interface BaseCachedModelRead : ModelRead, BaseCachedModelBase {

    override val mdb: ModelRead

    val cache: ModelCache

    val cacheCopyMaxSize: Int

    override fun <M : Any> get(key: Key<M>, vararg options: Options.Read): Sized<M>? {
        when {
            Cache.Skip in options -> {
                cache.evict(key)
                return mdb.get(key, *options)
            }

            Cache.Refresh in options -> {
                val sized = mdb.get(key, *options)
                if (sized != null)
                    cache.put(key.asHeapKey(), sized)
                return sized
            }

            else -> {
                @Suppress("UNCHECKED_CAST")
                val entry = cache.getOrRetrieveEntry(key.asHeapKey()) { mdb.get(key, *options) } as ObjectCache.Entry<M>
                if (entry is ObjectCache.Entry.Cached) {
                    return entry
                }
                return null
            }
        }
    }

    fun maxSize(options: Array<out Options.Read>): Int {
        val optMaxSize: Cache.CopyMaxSize? = options.invoke()
        return optMaxSize?.size ?: cacheCopyMaxSize
    }

    private fun <M : Any> wrapCursor(cursor: ModelCursor<M>, options: Array<out Options.Read>): ModelCursor<M> {
        return when {
            Cache.Skip in options -> cursor
            Cache.Refresh in options -> CachedModelCursor(cursor, ObjectCache(maxSize(options)))
            else -> CachedModelCursor(cursor, cache.newCopy(maxSize(options)))
        }
    }

    override fun findAll(vararg options: Options.Read): ModelCursor<*> = wrapCursor(mdb.findAll(*options), options)

    override fun <M : Any> findAllByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M>  = wrapCursor(mdb.findAllByType(type, *options), options)

    override fun <M : Any> findByPrimaryKey(type: KClass<M>, primaryKey: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M>  = wrapCursor(mdb.findByPrimaryKey(type, primaryKey, isOpen, *options), options)

    override fun <M : Any> findAllByIndex(type: KClass<M>, name: String, vararg options: Options.Read): ModelCursor<M>  = wrapCursor(mdb.findAllByIndex(type, name, *options), options)

    override fun <M : Any> findByIndex(type: KClass<M>, name: String, value: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M>  = wrapCursor(mdb.findByIndex(type, name, value, isOpen, *options), options)

    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String> = mdb.getIndexesOf(key, *options)

}
