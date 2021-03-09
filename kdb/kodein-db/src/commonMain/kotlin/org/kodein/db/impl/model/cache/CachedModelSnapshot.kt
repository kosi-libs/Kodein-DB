package org.kodein.db.impl.model.cache

import org.kodein.db.KeyMaker
import org.kodein.db.ValueMaker
import org.kodein.db.data.DataSnapshot
import org.kodein.db.model.ModelSnapshot
import org.kodein.db.model.cache.ModelCache
import org.kodein.memory.Closeable

internal class CachedModelSnapshot(override val mdb: ModelSnapshot, override val cache: ModelCache, override val copyMaxSize: Long)
    : CachedModelReadModule, ModelSnapshot, KeyMaker by mdb, ValueMaker by mdb, Closeable by mdb {
    override val data: DataSnapshot get() = mdb.data
}
