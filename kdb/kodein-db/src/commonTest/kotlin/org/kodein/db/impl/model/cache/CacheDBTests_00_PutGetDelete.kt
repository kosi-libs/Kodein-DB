package org.kodein.db.impl.model.cache

import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.db.model.delete
import org.kodein.db.model.get
import org.kodein.db.key
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame

@Suppress("ClassName")
class CacheDBTests_00_PutGetDelete : CacheDBTests() {

    @Test
    fun test00_PutGet() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.keyFrom(me)
        mdb.put(key, me)
        val otherMe = mdb[key]?.model
        assertSame(me, otherMe)
    }

    @Test
    fun test01_putGetCreateKey() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        mdb.put(me)
        val key = mdb.key<Adult>("BRYS", "Salomon")
        val otherMe = mdb[key]?.model
        assertSame(me, otherMe)
    }

    @Test
    fun test02_getNothing() {
        mdb.put(Adult("Salomon", "BRYS", Date(15, 12, 1986)))
        assertNull(mdb[mdb.key<Adult>("somebody", "else")])
    }

    @Test
    fun test03_deleteByKey() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.keyFrom(me)
        mdb.put(key, me)
        mdb.delete(key)
        assertNull(mdb[key])
    }

}
