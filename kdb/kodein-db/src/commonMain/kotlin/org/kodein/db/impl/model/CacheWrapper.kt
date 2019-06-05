//package org.kodein.db.impl.model
//
//import org.kodein.db.Options
//import org.kodein.db.Value
//import org.kodein.db.model.Cache
//import org.kodein.db.model.Key
//import org.kodein.db.model.ModelCursor
//import org.kodein.db.model.ModelDB
//import org.kodein.memory.model.ObjectCache
//import org.kodein.memory.model.Sized
//import kotlin.reflect.KClass
//
//class CacheWrapper(val mdb: ModelDB, val cache: ObjectCache<Key<*>, Any>) : ModelDB {
//
//    override fun <M : Any> getKey(type: KClass<M>, primaryKey: Value): Key<M> = mdb.getKey(type, primaryKey)
//
//    override fun <M : Any> getKey(model: M, vararg options: Options.Write): Key<M> = mdb.getKey(model, *options)
//
//    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String> = mdb.getIndexesOf(key, *options)
//
//    override fun close() = mdb.close()
//
//
//    override fun <M : Any> get(key: Key<M>, vararg options: Options.Read): Sized<M>? {
//        @Suppress("UNCHECKED_CAST")
//        val entry = cache.getOrRetrieveEntry(key) { mdb.get(key, *options) } as ObjectCache.Entry<M>
//        if (entry is ObjectCache.Entry.Cached) {
//            return entry
//        }
//        return null
//    }
//
//    override fun put(model: Any, vararg options: Options.Write): Int {
//        val (key, size) = mdb.putAndGetKey(model, *options)
//        if (Cache.Skip in options) {
//            cache.remove(key)
//        } else {
//            cache.put(key, model, size)
//        }
//        return size
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//    override fun newBatch(): ModelDB.Batch {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun newSnapshot(): ModelDB.Snapshot {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//
//    override fun <M : Any> putAndGetKey(model: M, vararg options: Options.Write): Key<M> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun delete(key: Key<*>, vararg options: Options.Write) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun findAll(vararg options: Options.Read): ModelCursor<*> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun <M : Any> findByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun <M : Any> findByPrimaryKey(type: KClass<M>, primaryKey: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun <M : Any> findAllByIndex(type: KClass<M>, name: String, vararg options: Options.Read): ModelCursor<M> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun <M : Any> findByIndex(type: KClass<M>, name: String, value: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//}
