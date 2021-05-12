package org.kodein.db.impl.model

import kotlinx.serialization.Serializable
import org.kodein.db.Anticipate
import org.kodein.db.model.delete
import org.kodein.db.model.get
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class ModelDBTests_09_Checks : ModelDBTests() {

    class LDB : ModelDBTests_09_Checks(), ModelDBTests.LDB
    class IM : ModelDBTests_09_Checks(), ModelDBTests.IM

    abstract class Encrypted : ModelDBTests_09_Checks(), ModelDBTests.Encrypted {
        class LDB : Encrypted(), ModelDBTests.LDB
        class IM : Encrypted(), ModelDBTests.IM
    }


    @Serializable
    private data class IntEntry(override val id: String, val value: Int) : Metadata

    @Test
    fun test00_putOK() {
        val int = IntEntry("test", 21)
        val key = mdb.keyFrom(int)
        mdb.put(key, int)

        mdb.put(key, int.copy(value = 42), Anticipate { check(mdb[key]!!.model.value == 21) })

        assertEquals(42, mdb[key]!!.model.value)
    }

    @Test
    fun test01_putKO() {
        val int = IntEntry("test", 21)
        val key = mdb.keyFrom(int)
        mdb.put(key, int)

        assertFailsWith<IllegalStateException> {
            mdb.put(key, int.copy(value = 42), Anticipate { check(mdb[key]!!.model.value == 0) })
        }

        assertEquals(21, mdb[key]!!.model.value)
    }

    @Test
    fun test02_deleteOK() {
        val int = IntEntry("test", 42)
        val key = mdb.keyFrom(int)
        mdb.put(key, int)

        mdb.delete(key, Anticipate { check(mdb[key]!!.model.value == 42) })

        assertNull(mdb[key])
    }

    @Test
    fun test03_deleteKO() {
        val int = IntEntry("test", 42)
        val key = mdb.keyFrom(int)
        mdb.put(key, int)

        assertFailsWith<IllegalStateException> {
            mdb.delete(key, Anticipate { check(mdb[key]!!.model.value == 0) })
        }

        assertEquals(42, mdb[key]!!.model.value)
    }

    @Test
    fun test04_batchOK() {
        val int = IntEntry("test", 21)
        val key = mdb.keyFrom(int)
        mdb.put(key, int)

        mdb.newBatch().use { batch ->
            batch.put(key, int.copy(value = 42))
            MaybeThrowable().also { batch.write(it, Anticipate { check(mdb[key]!!.model.value == 21) }) }.shoot()
        }

        assertEquals(42, mdb[key]!!.model.value)
    }

    @Test
    fun test05_batchKO() {
        val int = IntEntry("test", 21)
        val key = mdb.keyFrom(int)
        mdb.put(key, int)

        mdb.newBatch().use { batch ->
            batch.put(key, int)
            assertFailsWith<IllegalStateException> {
                MaybeThrowable().also { batch.write(it, Anticipate { check(mdb[key]!!.model.value == 0) }) }.shoot()
            }
        }

        assertEquals(21, mdb[key]!!.model.value)
    }

}
