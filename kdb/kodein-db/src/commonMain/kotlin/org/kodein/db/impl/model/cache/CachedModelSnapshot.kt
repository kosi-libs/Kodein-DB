package org.kodein.db.impl.model.cache

import org.kodein.db.model.ModelDB

class CachedModelSnapshot(override val mdb: ModelDB.Snapshot, override val cache: ModelCache, override val cacheCopyMaxSize: Int) : BaseCachedModelRead, ModelDB.Snapshot {
    override fun close()  = mdb.close()
}
