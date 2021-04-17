package org.kodein.db.impl.model.cache

import kotlinx.atomicfu.atomic
import org.kodein.db.Key
import org.kodein.db.Sized
import org.kodein.db.impl.utils.newRWLock
import org.kodein.db.impl.utils.withLock
import org.kodein.db.model.cache.BaseModelCache
import org.kodein.db.model.cache.ModelCache
import kotlin.jvm.Volatile

internal class ModelCacheImpl private constructor(private var internals: Internals, private val instanceMaxSize: Long) : ModelCache {

    constructor(maxSize: Long) : this(Internals(maxSize), maxSize)

    @Volatile
    private var internalsVersion = 0

    private class Internals(var maxSize: Long) {
        init {
            require(maxSize > 0) { "maxSize <= 0" }
        }

        val map = LinkedHashMap<Key<*>, ModelCache.Entry<*>>(0, 0.75f)
        val lock = newRWLock()
        var size = 0L
        val atomicHitCount = atomic(0)
        val atomicMissCount = atomic(0)
        var retrieveCount = 0
        var putCount = 0
        var deleteCount = 0
        var evictionCount = 0

        val refCount = atomic(1)
    }

    override val entryCount: Int get() = lockRead { internals.map.size }

    override val size: Long get() = lockRead { internals.size }
    override val maxSize: Long get() = lockRead { internals.maxSize }

    override val hitCount get() = lockRead { internals.atomicHitCount.value }
    override val missCount get() = lockRead { internals.atomicMissCount.value }
    override val retrieveCount get() = lockRead { internals.retrieveCount }
    override val putCount get() = lockRead { internals.putCount }
    override val deleteCount get() = lockRead { internals.deleteCount }
    override val evictionCount  get() = lockRead { internals.evictionCount }

    private fun Internals.unsafeTrimToSize(maxSize: Long = this.maxSize) {
        if (internals.refCount.value == 1)
            internals.maxSize = instanceMaxSize

        if (size <= maxSize || map.isEmpty())
            return

        val it = map.entries.iterator()
        while (true) {
            check(size >= 0) { "Cache size is $size" }
            check(!(size != 0L && !it.hasNext())) { "Cache is empty but size is $size" }

            if (size <= maxSize || !it.hasNext())
                break

            val toEvict = it.next()
            it.remove()
            size -= toEvict.value.size
            ++evictionCount
        }
    }

    private inline fun <T> lockRead(block: () -> T): T {
        while (true) {
            var trimNeeded = false
            val version = internalsVersion
            internals.lock.readLock().withLock {
                if (internalsVersion == version) {
                    val maxSize = if (internals.refCount.value == 1) instanceMaxSize else internals.maxSize
                    if (internals.maxSize != maxSize || internals.size > maxSize) {
                        trimNeeded = true
                    }
                    else {
                        return block()
                    }
                }
            }
            if (trimNeeded) {
                lockWrite {
                    internals.unsafeTrimToSize()
                }
                trimNeeded = false
            }
        }
    }

    private inline fun <T> lockWrite(block: () -> T): T {
        while (true) {
            val version = internalsVersion
            internals.lock.writeLock().withLock {
                if (internalsVersion == version) {
                    val ret = block()
                    internals.unsafeTrimToSize()
                    return ret
                }
            }
        }
    }

    private fun <M : Any> unsafeGetEntry(key: Key<M>) : ModelCache.Entry<M> {
        val entry = internals.map[key]
        if (entry != null) internals.atomicHitCount.incrementAndGet()
        else internals.atomicMissCount.incrementAndGet()
        @Suppress("UNCHECKED_CAST")
        return (entry ?: ModelCache.Entry.NotInCache) as ModelCache.Entry<M>
    }

    override fun <M : Any> getEntry(key: Key<M>): ModelCache.Entry<M> = lockRead { unsafeGetEntry(key) }

    private fun <M : Any> getOrRetrieveEntry(key: Key<M>, retrieve: () -> Sized<M>?, lockWrite: (() -> ModelCache.Entry<M>) -> ModelCache.Entry<M>): ModelCache.Entry<M> {
        val entry1 = lockRead { unsafeGetEntry(key) }
        if (entry1 !is ModelCache.Entry.NotInCache) return entry1

        return lockWrite {
            val entry2 = unsafeGetEntry(key)
            if (entry2 !is ModelCache.Entry.NotInCache) {
                entry2
            } else {
                copyIfNeeded()

                val sized = retrieve()

                @Suppress("UNCHECKED_CAST")
                val newEntry = if (sized != null) ModelCache.Entry.Cached(sized.model, sized.size) else ModelCache.Entry.Deleted as ModelCache.Entry<M>

                internals.map[key] = newEntry
                ++internals.retrieveCount

                newEntry
            }
        }
    }

    override fun <M : Any> getOrRetrieveEntry(key: Key<M>, retrieve: () -> Sized<M>?): ModelCache.Entry<M> = getOrRetrieveEntry(key, retrieve, this::lockWrite)

    private fun renewInternals(copy: Boolean) {
        internals.refCount.decrementAndGet()
        val newInternals = Internals(instanceMaxSize)
        if (copy)
            newInternals.map.putAll(internals.map)
        internals = newInternals
        ++internalsVersion
    }

    private fun copyIfNeeded() {
        val count = internals.refCount.value
        check(count >= 1) { "refCount < 1" }
        if (count == 1) return

        renewInternals(true)
    }

    private fun <M : Any> unsafePut(key: Key<M>, value: M, size: Int) {
        copyIfNeeded()

        val entry = ModelCache.Entry.Cached(value, size + 8)
        val previous = internals.map.put(key, entry)

        if (previous != null) {
            internals.size -= previous.size
        }

        ++internals.putCount
        internals.size += entry.size
    }

    override fun <M : Any> put(key: Key<M>, value: M, size: Int) {
        lockWrite {
            unsafePut(key, value, size)
        }
    }

    private fun <M : Any> unsafeDelete(key: Key<M>): ModelCache.Entry<M> {
        copyIfNeeded()

        @Suppress("UNCHECKED_CAST")
        val previous = internals.map.put(key, ModelCache.Entry.Deleted)

        internals.size += ModelCache.Entry.Deleted.size

        if (previous != null) {
            ++internals.deleteCount
            internals.size -= previous.size
        }

        @Suppress("UNCHECKED_CAST")
        return (previous ?: ModelCache.Entry.NotInCache) as ModelCache.Entry<M>
    }

    override fun <M : Any> delete(key: Key<M>): ModelCache.Entry<M> {
        lockWrite {
            return unsafeDelete(key)
        }
    }

    private fun <M : Any> unsafeEvict(key: Key<M>): ModelCache.Entry<M> {
        copyIfNeeded()

        @Suppress("UNCHECKED_CAST")
        val previous = internals.map.remove(key)

        if (previous != null) {
            ++internals.evictionCount
            internals.size -= previous.size
        }

        @Suppress("UNCHECKED_CAST")
        return (previous ?: ModelCache.Entry.NotInCache) as ModelCache.Entry<M>
    }

    override fun <M : Any> evict(key: Key<M>): ModelCache.Entry<M> {
        lockWrite {
            return unsafeEvict(key)
        }
    }

    override fun clear() {
        lockWrite {
            renewInternals(false)
        }
    }

    override fun batch(block: BaseModelCache.() -> Unit) {
        lockWrite {
            var batch: BaseModelCache? = object : BaseModelCache {
                override fun <M : Any> getEntry(key: Key<M>): ModelCache.Entry<M> = unsafeGetEntry(key)
                override fun <M : Any> getOrRetrieveEntry(key: Key<M>, retrieve: () -> Sized<M>?): ModelCache.Entry<M> = getOrRetrieveEntry(key, retrieve, ::run)
                override fun <M : Any> put(key: Key<M>, value: M, size: Int) = unsafePut(key, value, size)
                override fun <M : Any> delete(key: Key<M>): ModelCache.Entry<M> = unsafeDelete(key)
                override fun <M : Any> evict(key: Key<M>): ModelCache.Entry<M> = unsafeEvict(key)
                override fun clear() = renewInternals(false)
                override fun batch(block: BaseModelCache.() -> Unit) = throw IllegalStateException()
            }

            fun getBatch(): BaseModelCache = batch ?: throw IllegalStateException("Do not use this object outside of the batch function")

            val proxy = object : BaseModelCache {
                override fun <M : Any> getEntry(key: Key<M>): ModelCache.Entry<M> = getBatch().getEntry(key)
                override fun <M : Any> getOrRetrieveEntry(key: Key<M>, retrieve: () -> Sized<M>?): ModelCache.Entry<M> = getBatch().getOrRetrieveEntry(key, retrieve)
                override fun <M : Any> put(key: Key<M>, value: M, size: Int) = getBatch().put(key, value, size)
                override fun <M : Any> delete(key: Key<M>): ModelCache.Entry<M> = getBatch().delete(key)
                override fun <M : Any> evict(key: Key<M>): ModelCache.Entry<M> = getBatch().evict(key)
                override fun clear() = getBatch().clear()
                override fun batch(block: BaseModelCache.() -> Unit) = this.block()
            }

            proxy.block()

            batch = null
        }
    }

    override fun newCopy(copyMaxSize: Long): ModelCache {
        lockWrite {
            internals.refCount.incrementAndGet()
            return ModelCacheImpl(internals, copyMaxSize)
        }
    }

    override fun toString(): String {
        lockRead {
            val maxSize = internals.maxSize
            val useRate = if (size != 0L) 100 * size / maxSize else 0
            val hits = internals.atomicHitCount.value
            val misses = internals.atomicMissCount.value
            val accesses = hits + misses
            val hitRate = if (accesses != 0) 100 * hits / accesses else 0
            return "ModelCache[maxSize=$maxSize,size=$size,useRate=$useRate,hits=$hits,misses=$misses,hitRate=$hitRate%]"
        }
    }

}
