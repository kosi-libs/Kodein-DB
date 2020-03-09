package org.kodein.db.impl

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Birth
import org.kodein.db.impl.model.City
import org.kodein.db.model.cache.ModelCache

@Suppress("ClassName")
class DBTests_00_Find_NoCache : DBTests_00_Find() {
    override fun options(): Array<out Options.Open> = arrayOf(
            kxSerializer,
            ModelCache.Disable,
            TypeTable {
                root<Adult>()
                root<City>()
                root<Birth>()
            }
    )
}
