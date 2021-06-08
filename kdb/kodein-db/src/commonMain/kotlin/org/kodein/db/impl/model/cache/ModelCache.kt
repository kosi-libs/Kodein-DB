package org.kodein.db.impl.model.cache

import org.kodein.db.model.ModelDB
import org.kodein.db.model.cache.ModelCache

internal expect val defaultCacheSize: Long
internal fun defaultCacheCopyMaxSize() = defaultCacheSize / 4

public fun ModelCache.Companion.middleware(maxSize: Long = defaultCacheSize, copyMaxSize: Long = defaultCacheCopyMaxSize(), hashCodeImmutabilityChecks: Boolean = true): (ModelDB) -> ModelDB =
        { CachedModelDB(it, ModelCacheImpl(maxSize, hashCodeImmutabilityChecks), copyMaxSize) }
