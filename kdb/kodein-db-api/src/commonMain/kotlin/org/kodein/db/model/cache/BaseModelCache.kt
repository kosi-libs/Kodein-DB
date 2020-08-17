package org.kodein.db.model.cache

import org.kodein.db.Key
import org.kodein.db.Sized

public interface BaseModelCache {

    public fun <M : Any> getEntry(key: Key<M>): ModelCache.Entry<M>

    public operator fun <M : Any> get(key: Key<M>): M? = getEntry(key).model

    public fun <M : Any> getOrRetrieveEntry(key: Key<M>, retrieve: () -> Sized<M>?): ModelCache.Entry<M>

    public fun <M : Any> getOrRetrieve(key: Key<M>, retrieve: () -> Sized<M>?): M? = getOrRetrieveEntry(key, retrieve).model

    public fun <M : Any> put(key: Key<M>, value: M, size: Int)

    public fun <M : Any> put(key: Key<M>, sized: Sized<M>): Unit = put(key, sized.model, sized.size)

    public fun <M : Any> delete(key: Key<M>): ModelCache.Entry<M>

    public fun <M : Any> evict(key: Key<M>): ModelCache.Entry<M>

    public fun clear()

    public fun batch(block: BaseModelCache.() -> Unit)

}
