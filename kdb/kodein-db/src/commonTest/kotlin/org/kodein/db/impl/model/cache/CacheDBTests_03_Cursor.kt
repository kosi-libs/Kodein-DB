package org.kodein.db.impl.model.cache

import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.db.model.findAllByType
import org.kodein.db.model.get
import org.kodein.db.model.putAll
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class CacheDBTests_03_Cursor : CacheDBTests() {

    @Test
    fun test00_cursor() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val her = Adult("Laila", "ATIE", Date(25, 8, 1989))

        mdb.putAll(listOf(me, her))
        cache.clear()

        mdb[mdb.keyFrom(her)]

        mdb.findAllByType<Adult>().use {
            it as CachedModelCursor<*>
            assertEquals(1, cache.entryCount)
            assertEquals(1, it.cache.entryCount)

            it.model()
            assertEquals(1, cache.entryCount)
            assertEquals(1, it.cache.entryCount)

            it.next()
            it.model()
            assertEquals(1, cache.entryCount)
            assertEquals(2, it.cache.entryCount)
        }
    }

}
