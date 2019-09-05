package org.kodein.db.impl.model.cache

internal actual val defaultCacheSize: Long = Runtime.getRuntime().totalMemory() / 8
