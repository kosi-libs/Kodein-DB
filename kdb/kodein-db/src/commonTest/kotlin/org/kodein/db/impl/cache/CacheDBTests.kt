package org.kodein.db.impl.cache

import org.kodein.db.impl.model.ModelDBTests
import org.kodein.db.impl.model.cache.CachedModelDB
import org.kodein.db.impl.model.cache.ModelCacheImpl
import org.kodein.db.model.ModelDB
import org.kodein.db.model.cache.ModelCache
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class CacheDBTests : ModelDBTests() {

    private var _cache: ModelCache? = null
    protected val cache: ModelCache get() = _cache!!

    open fun testCache(): ModelCache = ModelCacheImpl(64 * 1024)

    open fun testCacheCopyMaxSize(): Long = 16 * 1024

    override fun newModelDB(): ModelDB = CachedModelDB(
            super.newModelDB(),
            cache,
            testCacheCopyMaxSize()
    )

    @BeforeTest
    override fun setUp() {
        _cache?.clear()
        _cache = testCache()
        super.setUp()
    }

    @AfterTest
    override fun tearDown() {
        _cache = null
        super.tearDown()
    }

}
