package org.kodein.db.impl.model.cache

import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.invoke
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.ModelIndexCursor
import org.kodein.db.model.ModelRead
import org.kodein.db.model.cache.ModelCache
import kotlin.reflect.KClass

internal interface CachedModelReadModule : ModelRead {

    val mdb: ModelRead

    val cache: ModelCache

    val copyMaxSize: Long

    override fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Get): Sized<M>? {
        when {
            ModelCache.Skip in options -> {
                cache.evict(key)
                @Suppress("ReplaceGetOrSet")
                return mdb.get(type, key, *options)
            }

            ModelCache.Refresh in options -> {
                @Suppress("ReplaceGetOrSet")
                val sized = mdb.get(type, key, *options)
                if (sized != null)
                    cache.put(key, sized)
                return sized
            }

            else -> {
                @Suppress("UNCHECKED_CAST")
                val entry = cache.getOrRetrieveEntry(key) {
                    @Suppress("ReplaceGetOrSet")
                    mdb.get(type, key, *options)
                }
                if (entry is ModelCache.Entry.Cached) {
                    return entry
                }
                return null
            }
        }
    }

    fun maxSize(options: Array<out Options>): Long {
        val optMaxSize: ModelCache.CopyMaxSize? = options.invoke()
        return optMaxSize?.maxSize ?: copyMaxSize
    }

    private fun <M : Any> wrapCursor(cursor: ModelCursor<M>, options: Array<out Options.Find>): ModelCursor<M> {
        return when {
            ModelCache.Skip in options -> cursor
            ModelCache.Refresh in options -> CachedModelCursor(cursor, ModelCacheImpl(maxSize(options), cache.hashCodeImmutabilityChecks))
            else -> CachedModelCursor(cursor, cache.newCopy(maxSize(options)))
        }
    }

    private fun <M : Any> wrapIndexCursor(cursor: ModelIndexCursor<M>, options: Array<out Options.Find>): ModelIndexCursor<M> {
        return when {
            ModelCache.Skip in options -> cursor
            ModelCache.Refresh in options -> CachedModelIndexCursor(cursor, ModelCacheImpl(maxSize(options), cache.hashCodeImmutabilityChecks))
            else -> CachedModelIndexCursor(cursor, cache.newCopy(maxSize(options)))
        }
    }

    override fun findAll(vararg options: Options.Find): ModelCursor<*> = wrapCursor(mdb.findAll(*options), options)

    override fun <M : Any> findAllByType(type: KClass<M>, vararg options: Options.Find): ModelCursor<M> = wrapCursor(mdb.findAllByType(type, *options), options)

    override fun <M : Any> findById(type: KClass<M>, id: Any, isOpen: Boolean, vararg options: Options.Find): ModelCursor<M> = wrapCursor(mdb.findById(type, id, isOpen, *options), options)

    override fun <M : Any> findAllByIndex(type: KClass<M>, index: String, vararg options: Options.Find): ModelIndexCursor<M> = wrapIndexCursor(mdb.findAllByIndex(type, index, *options), options)

    override fun <M : Any> findByIndex(type: KClass<M>, index: String, value: Any, isOpen: Boolean, vararg options: Options.Find): ModelIndexCursor<M> = wrapIndexCursor(mdb.findByIndex(type, index, value, isOpen, *options), options)

    override fun getIndexesOf(key: Key<*>): Set<String> = mdb.getIndexesOf(key)

}
