package org.kodein.db.impl.cache

import org.kodein.db.impl.model.ModelDBTests
import org.kodein.db.impl.model.cache.CachedModelDB
import org.kodein.db.impl.model.cache.ModelCache
import org.kodein.db.model.ModelDB

abstract class CacheDBTests : ModelDBTests() {

    private var _cache: ModelCache? = null
    protected val cache: ModelCache get() = _cache!!

    open fun testCache(): ModelCache = ModelCache(64 * 1024)

    open fun testCacheCopyMaxSize(): Int = 16 * 1024

    override fun newModelDB(): ModelDB = CachedModelDB(
            super.newModelDB(),
            cache,
            testCacheCopyMaxSize()
    )

    override fun setUp() {
        _cache?.clean()
        _cache = testCache()
        super.setUp()
    }

    override fun tearDown() {
        _cache = null
        super.tearDown()
    }

}
