package org.kodein.db.impl.cache

import org.kodein.db.Value
import org.kodein.db.getHeapKey
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

@Suppress("ClassName")
class CacheDBTests_00_PutGetDelete : CacheDBTests() {

    @Test
    fun test00_PutGet() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.putAndGetHeapKey(me).value
        val otherMe = mdb[key]?.value
        assertSame(me, otherMe)
    }

    @Test
    fun test01_putGetCreateKey() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        mdb.put(me)
        val key = mdb.getHeapKey<Adult>(Value.ofAscii("BRYS", "Salomon"))
        val otherMe = mdb[key]?.value
        assertSame(me, otherMe)
    }

    @Test
    fun test02_getNothing() {
        mdb.put(Adult("Salomon", "BRYS", Date(15, 12, 1986)))
        assertNull(mdb[mdb.getHeapKey<Adult>(Value.ofAscii("somebody", "else"))])
    }

    @Test
    fun test03_deleteByKey() {
        val key = mdb.putAndGetHeapKey(Adult("Salomon", "BRYS", Date(15, 12, 1986))).value
        mdb.delete(key)
        assertNull(mdb[key])
    }

}
