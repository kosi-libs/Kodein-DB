package org.kodein.db.impl.model.cache

import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.db.impl.model.default
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.cache.ModelCache
import org.kodein.db.model.get
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
abstract class CacheDBTests_04_Options : CacheDBTests() {

    class LDB : CacheDBTests_04_Options() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : CacheDBTests_04_Options() { override val factory = ModelDB.inMemory }


    @Test
    fun test00_putSkip() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        assertEquals(0, cache.entryCount)
        val key = mdb.keyFrom(me)
        mdb.put(key, me, ModelCache.Skip)
        assertEquals(0, cache.entryCount)
        val otherMe = mdb[key]!!.model
        assertNotSame(me, otherMe)
        assertEquals(1, cache.entryCount)
        assertSame(otherMe, mdb[key]!!.model)
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
        val key = mdb.keyFrom(me)
        mdb.put(me)

        assertSame(me, mdb[key]!!.model)

        val otherMe = mdb[key, ModelCache.Refresh]!!.model

        assertNotSame(me, otherMe)
        assertSame(otherMe, mdb[key]!!.model)
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
