package org.kodein.db.impl.model.cache

import org.kodein.db.model.ModelDB
import org.kodein.db.model.cache.ModelCache

internal expect val defaultCacheSize: Long

fun ModelCache.Companion.middleware(maxSize: Long = defaultCacheSize, cacheCopyMaxSize: Long = defaultCacheSize / 4): (ModelDB) -> ModelDB =
        { CachedModelDB(it, ModelCacheImpl(maxSize), cacheCopyMaxSize) }
