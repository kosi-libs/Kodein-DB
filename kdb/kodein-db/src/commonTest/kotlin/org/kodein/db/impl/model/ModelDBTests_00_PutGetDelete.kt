package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.model.delete
import org.kodein.db.model.get
import org.kodein.db.key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

@Suppress("ClassName")
open class ModelDBTests_00_PutGetDelete : ModelDBTests() {

    @Test
    fun test00_putGetByKey() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.keyFrom(me)
        mdb.put(key, me)
        val otherMe = mdb[key]?.model
        assertEquals(me, otherMe)
        assertNotSame(me, otherMe)
    }

    @Test
    fun test01_putGetCreateKey() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        mdb.put(me)
        val key = mdb.key<Adult>(Value.ofAscii("BRYS", "Salomon"))
        val otherMe = mdb[key]?.model
        assertEquals(me, otherMe)
        assertNotSame(me, otherMe)
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

    @Test
    fun test04_deleteCreateKey() {
        mdb.put(Adult("Salomon", "BRYS", Date(15, 12, 1986)))
        val key = mdb.key<Adult>("BRYS", "Salomon")
        mdb.delete(key)
        assertNull(mdb[key])
    }

}
