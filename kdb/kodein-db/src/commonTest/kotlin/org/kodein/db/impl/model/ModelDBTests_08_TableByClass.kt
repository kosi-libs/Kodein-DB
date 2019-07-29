package org.kodein.db.impl.model

import org.kodein.db.model.Serializer
import org.kodein.memory.io.putTable
import org.kodein.memory.io.readTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

@Suppress("ClassName")
class ModelDBTests_08_TableByClass : ModelDBTests() {

    override fun testSerializer() = Serializer.ByClass {
        +Serializer<Adult>(
                {
                    putTable {
                        string("firstName", it.firstName)
                        string("lastName", it.lastName)
                        int("birth_day", it.birth.day)
                        int("birth_month", it.birth.month)
                        int("birth_year", it.birth.year)
                    }
                },
                { _ ->
                    readTable().let {
                        Adult(it.string("firstName"), it.string("lastName"), Date(it.int("birth_day"), it.int("birth_month"), it.int("birth_year")))
                    }
                }
        )
    }

    @Test
    fun test00_Table() {

        val me = Adult("Salomon", "BRYS", Date(15, 12, 1986))

        val key = mdb.putAndGetHeapKey(me).value

        val me2 = mdb[key]!!.value

        assertEquals(me, me2)
        assertNotSame(me, me2)
    }

}
