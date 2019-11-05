package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.impl.Check
import org.kodein.db.model.Primitive
import org.kodein.db.model.delete
import org.kodein.db.model.get
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Suppress("ClassName")
class ModelDBTests_09_Checks : ModelDBTests() {

    @Test
    fun test00_putOK() {
        val int = Primitive(Value.ofAscii("test"), 21)
        val key = mdb.newHeapKey(int)
        mdb.put(key, int)

        mdb.put(key, int.copy(value = 42), Check { check(mdb[key]!!.value.value == 21) })

        assertEquals(42, mdb[key]!!.value.value)
    }

    @Test
    fun test01_putKO() {
        val int = Primitive(Value.ofAscii("test"), 21)
        val key = mdb.newHeapKey(int)
        mdb.put(key, int)

        assertFailsWith<IllegalStateException> {
            mdb.put(key, int.copy(value = 42), Check { check(mdb[key]!!.value.value == 0) })
        }

        assertEquals(21, mdb[key]!!.value.value)
    }

    @Test
    fun test02_deleteOK() {
        val int = Primitive(Value.ofAscii("test"), 42)
        val key = mdb.newHeapKey(int)
        mdb.put(key, int)

        mdb.delete(key, Check { check(mdb[key]!!.value.value == 42) })

        assertNull(mdb[key])
    }

    @Test
    fun test03_deleteKO() {
        val int = Primitive(Value.ofAscii("test"), 42)
        val key = mdb.newHeapKey(int)
        mdb.put(key, int)

        assertFailsWith<IllegalStateException> {
            mdb.delete(key, Check { check(mdb[key]!!.value.value == 0) })
        }

        assertEquals(42, mdb[key]!!.value.value)
    }

    @Test
    fun test04_batchOK() {
        val int = Primitive(Value.ofAscii("test"), 21)
        val key = mdb.newHeapKey(int)
        mdb.put(key, int)

        mdb.newBatch().use {
            it.put(key, int.copy(value = 42))
            it.write(Check { check(mdb[key]!!.value.value == 21) })
        }

        assertEquals(42, mdb[key]!!.value.value)
    }

    @Test
    fun test05_batchKO() {
        val int = Primitive(Value.ofAscii("test"), 21)
        val key = mdb.newHeapKey(int)
        mdb.put(key, int)

        mdb.newBatch().use {
            it.put(key, int)
            assertFailsWith<IllegalStateException> {
                it.write(Check { check(mdb[key]!!.value.value == 0) })
            }
        }

        assertEquals(21, mdb[key]!!.value.value)
    }

}
