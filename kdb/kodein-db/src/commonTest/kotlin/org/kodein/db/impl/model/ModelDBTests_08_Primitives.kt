@file:Suppress("DEPRECATION")

package org.kodein.db.impl.model

import org.kodein.db.model.Primitive
import org.kodein.db.model.get
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.toArrayMemory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame

@Suppress("ClassName")
abstract class ModelDBTests_08_Primitives : ModelDBTests() {

    class LDB : ModelDBTests_08_Primitives(), ModelDBTests.LDB
    class IM : ModelDBTests_08_Primitives(), ModelDBTests.IM

    @Test
    fun test00_Int() {
        val int = Primitive("test", 123456789)
        val key = mdb.put(int).key
        val other = mdb[key]!!.model
        assertNotSame(int, other)
        if (this !is Encrypted) {
            assertNotSame(int.id, other.id)
            assertEquals(mdb.valueOf(int.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        } else {
            assertNotEquals(mdb.valueOf(int.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        }
        assertEquals(int.value, other.value)
    }

    @Test
    fun test01_Long() {
        val long = Primitive("test", 1234567890123456789L)
        val key = mdb.put(long).key
        val other = mdb[key]!!.model
        assertNotSame(long, other)
        if (this !is Encrypted) {
            assertNotSame(long.id, other.id)
            assertEquals(mdb.valueOf(long.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        } else {
            assertNotEquals(mdb.valueOf(long.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        }
        assertEquals(long.value, other.value)
    }

    @Test
    fun test02_Double() {
        val double = Primitive("test", 123456789.0123456789)
        val key = mdb.put(double).key
        val other = mdb[key]!!.model
        assertNotSame(double, other)
        if (this !is Encrypted) {
            assertNotSame(double.id, other.id)
            assertEquals(mdb.valueOf(double.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        } else {
            assertNotEquals(mdb.valueOf(double.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        }
        assertEquals(double.value, other.value)
    }

    @Test
    fun test03_String() {
        val string = Primitive("test", "Salomon")
        val key = mdb.put(string).key
        val other = mdb[key]!!.model
        assertNotSame(string, other)
        if (this !is Encrypted) {
            assertNotSame(string.id, other.id)
            assertEquals(mdb.valueOf(string.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        } else {
            assertNotEquals(mdb.valueOf(string.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        }
        assertEquals(string.value, other.value)
    }

    @Test
    fun test04_Bytes() {
        val bytes = Primitive("test", byteArrayOf(0, 1, 2, 3, 4, 5))
        val key = mdb.put(bytes).key
        val other = mdb[key]!!.model
        assertNotSame(bytes, other)
        if (this !is Encrypted) {
            assertNotSame(bytes.id, other.id)
            assertEquals(mdb.valueOf(bytes.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        } else {
            assertNotEquals(mdb.valueOf(bytes.id).toArrayMemory(), mdb.valueOf(other.id).toArrayMemory())
        }
        assertBytesEquals(bytes.value, other.value)
    }
}
