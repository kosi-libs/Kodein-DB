package org.kodein.db.impl.cache

import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

@Suppress("ClassName")
class CacheDBTests_02_batch : CacheDBTests() {

    @Test
    fun test00_BatchPut() {

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.getHeapKey(me)

        mdb.newBatch().use {
            it.put(me)

            assertNull(mdb[key])

            it.write()
        }

        assertSame(me, mdb[key]!!.value)
    }

    @Test
    fun test01_BatchDelete() {

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.putAndGetHeapKey(me).value

        mdb.newBatch().use {
            it.delete(key)

            assertNotNull(mdb[key])

            it.write()
        }

        assertNull(mdb[key])
    }

}
