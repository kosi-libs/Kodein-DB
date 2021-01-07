package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.Primitive
import org.kodein.db.model.get
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.toArrayBuffer
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.array
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

@Suppress("ClassName")
abstract class ModelDBTests_08_Primitives : ModelDBTests() {

    class LDB : ModelDBTests_08_Primitives() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : ModelDBTests_08_Primitives() { override val factory = ModelDB.inMemory }

    @Test
    fun test00_Int() {
        val int = Primitive("test", 123456789)
        val key = mdb.put(int).key
        val other = mdb[key]!!.model
        assertNotSame(int, other)
        assertNotSame(int.id, other.id)
        assertEquals(mdb.valueOf(int.id).toArrayBuffer(), mdb.valueOf(other.id).toArrayBuffer())
        assertEquals(int.value, other.value)
    }

    @Test
    fun test01_Long() {
        val long = Primitive("test", 1234567890123456789L)
        val key = mdb.put(long).key
        val other = mdb[key]!!.model
        assertNotSame(long, other)
        assertNotSame(long.id, other.id)
        assertEquals(mdb.valueOf(long.id).toArrayBuffer(), mdb.valueOf(other.id).toArrayBuffer())
        assertEquals(long.value, other.value)
    }

    @Test
    fun test02_Double() {
        val double = Primitive("test", 123456789.0123456789)
        val key = mdb.put(double).key
        val other = mdb[key]!!.model
        assertNotSame(double, other)
        assertNotSame(double.id, other.id)
        assertEquals(mdb.valueOf(double.id).toArrayBuffer(), mdb.valueOf(other.id).toArrayBuffer())
        assertEquals(double.value, other.value)
    }

    @Test
    fun test03_String() {
        val string = Primitive("test", "Salomon")
        val key = mdb.put(string).key
        val other = mdb[key]!!.model
        assertNotSame(string, other)
        assertNotSame(string.id, other.id)
        assertEquals(mdb.valueOf(string.id).toArrayBuffer(), mdb.valueOf(other.id).toArrayBuffer())
        assertEquals(string.value, other.value)
    }

    @Test
    fun test04_Bytes() {
        val bytes = Primitive("test", byteArrayOf(0, 1, 2, 3, 4, 5))
        val key = mdb.put(bytes).key
        val other = mdb[key]!!.model
        assertNotSame(bytes, other)
        assertNotSame(bytes.id, other.id)
        assertEquals(mdb.valueOf(bytes.id).toArrayBuffer(), mdb.valueOf(other.id).toArrayBuffer())
        assertBytesEquals(bytes.value, other.value)
    }
}
