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
        @Suppress("UNCHECKED_CAST")
        val entry = cache.getOrRetrieveEntry(key) { mdb.get(key, *options) } as ObjectCache.Entry<M>
        if (entry is ObjectCache.Entry.Cached) {
            return entry
        }
        return null
    }

    fun maxSize(options: Array<out Options.Read>): Int {
        val optMaxSize: Cache.CopyMaxSize? = options.invoke()
        return optMaxSize?.size ?: cacheCopyMaxSize
    }

    override fun findAll(vararg options: Options.Read): ModelCursor<*> = CachedModelCursor(mdb.findAll(*options), cache.newCopy(maxSize(options)))

    override fun <M : Any> findByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M>  = CachedModelCursor(mdb.findByType(type, *options), cache.newCopy(maxSize(options)))

    override fun <M : Any> findByPrimaryKey(type: KClass<M>, primaryKey: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M>  = CachedModelCursor(mdb.findByPrimaryKey(type, primaryKey, isOpen, *options), cache.newCopy(maxSize(options)))

    override fun <M : Any> findAllByIndex(type: KClass<M>, name: String, vararg options: Options.Read): ModelCursor<M>  = CachedModelCursor(mdb.findAllByIndex(type, name, *options), cache.newCopy(maxSize(options)))

    override fun <M : Any> findByIndex(type: KClass<M>, name: String, value: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M>  = CachedModelCursor(mdb.findByIndex(type, name, value, isOpen, *options), cache.newCopy(maxSize(options)))

    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String> = mdb.getIndexesOf(key, *options)

}
