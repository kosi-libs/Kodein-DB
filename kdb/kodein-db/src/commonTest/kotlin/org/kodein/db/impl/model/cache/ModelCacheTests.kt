package org.kodein.db.impl.model.cache

import org.kodein.db.Key
import org.kodein.db.Sized
import org.kodein.db.Value
import org.kodein.db.impl.data.putDocumentKey
import org.kodein.db.model.cache.ModelCache
import org.kodein.memory.io.SliceBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.asserter

class ModelCacheTests  {

    private inline fun <reified T : Any> assertIs(value: Any) = asserter.assertTrue({ "Expected $value to be of type ${T::class} but is ${value::class}" }, value is T)

    @Test
    fun putGetDeleteRemove() {
        val cache = ModelCacheImpl(1024)

        val key = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("name")) })

        assertEquals(0, cache.entryCount)
        assertEquals(0, cache.missCount)
        assertIs<ModelCache.Entry.NotInCache>(cache.getEntry(key))
        assertEquals(1, cache.missCount)
        assertNull(cache[key])
        assertEquals(2, cache.missCount)

        assertEquals(0, cache.entryCount)
        assertEquals(0, cache.putCount)
        cache.put(key, "Salomon", 7)
        assertEquals(1, cache.entryCount)
        assertEquals(1, cache.putCount)
        assertEquals(0, cache.hitCount)
        assertIs<ModelCache.Entry.Cached<*>>(cache.getEntry(key))
        assertEquals(1, cache.hitCount)
        assertEquals("Salomon", cache[key])
        assertEquals(2, cache.hitCount)
        assertEquals(15, cache.size)

        assertEquals(0, cache.deleteCount)
        cache.delete(key)
        assertEquals(1, cache.entryCount)
        assertEquals(1, cache.deleteCount)
        assertIs<ModelCache.Entry.Deleted>(cache.getEntry(key))
        assertEquals(3, cache.hitCount)
        assertNull(cache[key])
        assertEquals(4, cache.hitCount)
        assertEquals(8, cache.size)

        assertEquals(0, cache.evictionCount)
        cache.evict(key)
        assertEquals(0, cache.entryCount)
        assertEquals(1, cache.evictionCount)
        assertIs<ModelCache.Entry.NotInCache>(cache.getEntry(key))
        assertEquals(3, cache.missCount)
        assertNull(cache[key])
        assertEquals(4, cache.missCount)
        assertEquals(0, cache.size)
    }

    @Test
    fun getOrRetrieve() {
        val cache = ModelCacheImpl(1024)

        val key = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("name")) })

        assertEquals(0, cache.retrieveCount)
        cache.getOrRetrieve(key) { Sized("Salomon", 7) }
        assertEquals(1, cache.retrieveCount)
        assertEquals("Salomon", cache[key])
    }

    @Test
    fun evict() {
        val k1 = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("1")) })
        val k2 = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("2")) })

        val cache = ModelCacheImpl(100)
        cache.put(k1, "O", 50)
        assertEquals("O", cache[k1])
        cache.put(k2, "T", 50)
        assertEquals("T", cache[k2])
        assertNull(cache[k1])
        assertIs<ModelCache.Entry.NotInCache>(cache.getEntry(k1))
    }

    @Test
    fun copyPutInCopy() {
        val me = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("me")) })
        val her = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("her")) })


        val cache = ModelCacheImpl(1024)
        cache.put(me, "Salomon", 7)

        val copy = cache.newCopy(512)

        assertEquals("Salomon", copy[me])
        assertEquals(1024, copy.maxSize)

        copy.put(her, "Laila", 5)
        assertEquals("Laila", copy[her])
        assertNull(cache[her])
        assertEquals(512, copy.maxSize)
    }

    @Test
    fun copyPutInOriginal() {
        val me = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("me")) })
        val her = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("her")) })

        val cache = ModelCacheImpl(1024)
        cache.put(me, "Salomon", 7)

        val copy = cache.newCopy(512)

        assertEquals("Salomon", copy[me])
        assertEquals(1024, copy.maxSize)

        cache.put(her, "Laila", 5)
        assertEquals("Laila", cache[her])
        assertNull(copy[her])
        assertEquals(512, copy.maxSize)
    }

    @Test
    fun clean() {
        val me = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("me")) })
        val her = Key<String>(SliceBuilder.array(1024).newSlice { putDocumentKey(1, Value.ofAscii("her")) })

        val cache = ModelCacheImpl(1024)
        cache.put(me, "Salomon", 7)
        cache.put(her, "laila", 5)

        assertEquals(2, cache.entryCount)
        assertEquals(28, cache.size)

        cache.clear()

        assertEquals(0, cache.entryCount)
        assertEquals(0, cache.size)
    }
}
