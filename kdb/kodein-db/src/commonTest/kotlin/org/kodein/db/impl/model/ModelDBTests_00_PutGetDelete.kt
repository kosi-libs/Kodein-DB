package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.model.getHeapKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

@Suppress("ClassName")
open class ModelDBTests_00_PutGetDelete : ModelDBTests() {

    @Test
    fun test00_putGetByKey() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.putAndGetHeapKey(me).value
        val otherMe = mdb[key]?.value
        assertEquals(me, otherMe)
        assertNotSame(me, otherMe)
    }

    @Test
    fun test01_putGetCreateKey() {
        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        mdb.put(me)
        val key = mdb.getHeapKey<Adult>(Value.ofAscii("BRYS", "Salomon"))
        val otherMe = mdb[key]?.value
        assertEquals(me, otherMe)
        assertNotSame(me, otherMe)
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

    @Test
    fun test04_deleteCreateKey() {
        mdb.put(Adult("Salomon", "BRYS", Date(15, 12, 1986)))
        val key = mdb.getHeapKey<Adult>(Value.ofAscii("BRYS", "Salomon"))
        mdb.delete(key)
        assertNull(mdb[key])
    }

}
