package org.kodein.db.impl.data

import org.kodein.db.Anticipate
import org.kodein.db.Value
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Suppress("ClassName")
class DataDBTests_12_Checks : DataDBTests() {

    @Test
    fun test00_putOK() {
        val key = ddb.newKey("int", Value.ofAscii("test"))

        ddb.put(key, Value.of(21))
        ddb.put(key, Value.of(42), emptySet(), Anticipate { check(ddb.get(key)!!.readInt() == 21) })

        assertEquals(42, ddb.get(key)!!.readInt())
    }

    @Test
    fun test01_putKO() {
        val key = ddb.newKey("int", Value.ofAscii("test"))

        ddb.put(key, Value.of(21))
        assertFailsWith<IllegalStateException> {
            ddb.put(key, Value.of(42), emptySet(), Anticipate { check(ddb.get(key)!!.readInt() == 0) })
        }

        assertEquals(21, ddb.get(key)!!.readInt())
    }

    @Test
    fun test02_deleteOK() {
        val key = ddb.newKey("int", Value.ofAscii("test"))
        ddb.put(key, Value.of(42))

        ddb.delete(key, Anticipate { check(ddb.get(key)!!.readInt() == 42) })

        assertNull(ddb.get(key))
    }

    @Test
    fun test03_deleteKO() {
        val key = ddb.newKey("int", Value.ofAscii("test"))
        ddb.put(key, Value.of(42))

        assertFailsWith<IllegalStateException> {
            ddb.delete(key, Anticipate { check(ddb.get(ddb.newKey("int", Value.ofAscii("test")))!!.readInt() == 0) })
        }

        assertEquals(42, ddb.get(key)!!.readInt())
    }

    @Test
    fun test04_batchOK() {
        val key = ddb.newKey("int", Value.ofAscii("test"))
        ddb.put(key, Value.of(21))

        ddb.newBatch().use { batch ->
            batch.put(key, Value.of(42))
            MaybeThrowable().also { batch.write(it, Anticipate { check(ddb.get(key)!!.readInt() == 21) }) }.shoot()
        }

        assertEquals(42, ddb.get(key)!!.readInt())
    }

    @Test
    fun test05_batchKO() {
        val key = ddb.newKey("int", Value.ofAscii("test"))
        ddb.put(key, Value.of(21))

        ddb.newBatch().use { batch ->
            batch.put(key, Value.of(42))
            assertFailsWith<IllegalStateException> {
                MaybeThrowable().also { batch.write(it, Anticipate { check(ddb.get(key)!!.readInt() == 0) }) }.shoot()
            }
        }

        assertEquals(21, ddb.get(key)!!.readInt())
    }

}
