package org.kodein.db.impl.cache

import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.db.impl.model.cache.CachedModelCursor
import org.kodein.db.model.cache.ModelCache
import org.kodein.db.model.get
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
class CacheDBTests_04_Options : CacheDBTests() {

    @Test
    fun test00_putSkip() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        assertEquals(0, cache.entryCount)
        val key = mdb.newHeapKey(me)
        mdb.put(key, me, ModelCache.Skip)
        assertEquals(0, cache.entryCount)
        val otherMe = mdb[key]!!.value
        assertNotSame(me, otherMe)
        assertEquals(1, cache.entryCount)
        assertSame(otherMe, mdb[key]!!.value)
        mdb.put(me, ModelCache.Skip)
        assertEquals(0, cache.entryCount)
    }

    @Test
    fun test01_cursorSkip() {
        mdb.findAll().use {
            assertTrue(it is CachedModelCursor<*>)
        }

        mdb.findAll(ModelCache.Skip).use {
            assertFalse(it is CachedModelCursor<*>)
        }
    }

    @Test
    fun test02_putRefresh() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.newHeapKey(me)
        mdb.put(me)

        assertSame(me, mdb[key]!!.value)

        val otherMe = mdb.get(key, ModelCache.Refresh)!!.value

        assertNotSame(me, otherMe)
        assertSame(otherMe, mdb[key]!!.value)
    }

    @Test
    fun test03_cursorRefresh() {
        mdb.put(Adult("Salomon", "BRYS", Date(15, 12, 1986)))
        assertEquals(1, cache.entryCount)

        mdb.findAll(ModelCache.Refresh).use {
            assertEquals(0, (it as CachedModelCursor).cache.entryCount)
        }
    }

}
