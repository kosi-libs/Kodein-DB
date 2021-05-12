package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataDB
import org.kodein.db.encryption.Encryption
import org.kodein.db.impl.data.DataDBTests_00_Put
import org.kodein.db.impl.data.default
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.delete
import org.kodein.db.model.get
import org.kodein.db.model.ModelDB
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import org.kodein.memory.text.toHex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class ModelDBTests_00_PutGetDelete : ModelDBTests() {

    class LDB : ModelDBTests_00_PutGetDelete(), ModelDBTests.LDB
    class IM : ModelDBTests_00_PutGetDelete(), ModelDBTests.IM

    abstract class Encrypted : ModelDBTests_00_PutGetDelete(), ModelDBTests.Encrypted {
        class LDB : Encrypted(), ModelDBTests.LDB
        class IM : Encrypted(), ModelDBTests.IM
    }

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
        val key = mdb.keyById<Adult>(Value.of("BRYS", "Salomon"))
        val otherMe = mdb[key]?.model
        assertEquals(me, otherMe)
        assertNotSame(me, otherMe)
    }

    @Test
    fun test02_getNothing() {
        mdb.put(Adult("Salomon", "BRYS", Date(15, 12, 1986)))
        assertNull(mdb[mdb.keyById<Adult>("somebody", "else")])
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
        val key = mdb.keyById<Adult>("BRYS", "Salomon")
        mdb.delete(key)
        assertNull(mdb[key])
    }

}
