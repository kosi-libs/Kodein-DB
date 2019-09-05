package org.kodein.db.impl.cache

import org.kodein.db.Options
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.db.Key
import org.kodein.db.DBListener
import org.kodein.db.model.orm.Metadata
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

@Suppress("ClassName")
class CacheDBTests_01_React : CacheDBTests() {

    @Test
    fun test00_ReactDidPutException() {
        val listener = object : DBListener<Any> {
            override fun didPut(model: Any, getKey: () -> Key<*>, typeName: String, metadata: Metadata, size: Int, options: Array<out Options.Write>) = throw IllegalStateException()
        }

        mdb.register(listener)

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))

        assertFailsWith<IllegalStateException> {
            mdb.put(me)
        }

        assertSame(me, mdb[mdb.getHeapKey(me)]!!.value)
    }

    @Test
    fun test01_ReactDidDeleteException() {
        val listener = object : DBListener<Any> {
            override fun didDelete(key: Key<*>, model: Any?, typeName: String, options: Array<out Options.Write>) = throw IllegalStateException()
        }

        mdb.register(listener)

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))
        val key = mdb.putAndGetHeapKey(me).value

        assertFailsWith<IllegalStateException> {
            mdb.delete(key)
        }

        assertNull(mdb[key])

    }

}
