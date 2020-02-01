package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.model.Primitive
import org.kodein.db.model.get
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.array
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

@Suppress("ClassName")
open class ModelDBTests_08_Primitives : ModelDBTests() {

    @Test
    fun test00_Int() {
        val int = Primitive(Value.ofAscii("test"), 123456789)
        val key = mdb.put(int).key
        val other = mdb[key]!!.model
        assertNotSame(int, other)
        assertBytesEquals(KBuffer.array(int.id.size) { int.id.writeInto(this) }, KBuffer.array(other.id.size) { other.id.writeInto(this) })
        assertEquals(int, other)
    }

    @Test
    fun test01_Long() {
        val long = Primitive(Value.ofAscii("test"), 1234567890123456789L)
        val key = mdb.put(long).key
        val other = mdb[key]!!.model
        assertNotSame(long, other)
        assertEquals(long, other)
    }

    @Test
    fun test02_Double() {
        val double = Primitive(Value.ofAscii("test"), 123456789.0123456789)
        val key = mdb.put(double).key
        val other = mdb[key]!!.model
        assertNotSame(double, other)
        assertEquals(double, other)
    }

}
