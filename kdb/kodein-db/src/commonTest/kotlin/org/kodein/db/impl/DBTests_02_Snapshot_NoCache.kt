package org.kodein.db.impl

import org.kodein.db.Options
import org.kodein.db.model.cache.ModelCache

@Suppress("ClassName")
class DBTests_02_Snapshot_NoCache : DBTests_02_Snapshot() {
    override fun options(): Array<out Options.Open> = arrayOf(kxSerializer, ModelCache.Disable)
}
