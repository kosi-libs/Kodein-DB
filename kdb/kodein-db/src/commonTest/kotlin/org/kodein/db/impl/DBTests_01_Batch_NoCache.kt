package org.kodein.db.impl

import org.kodein.db.Options
import org.kodein.db.model.cache.ModelCache

@Suppress("ClassName")
class DBTests_01_Batch_NoCache : DBTests_01_Batch() {
    override fun options(): Array<out Options.Open> = arrayOf(kxSerializer, ModelCache.Disable)
}
