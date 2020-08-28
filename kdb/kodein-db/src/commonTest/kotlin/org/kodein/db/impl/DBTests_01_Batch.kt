package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.impl.model.Message
import org.kodein.db.model.Primitive
import org.kodein.memory.util.UUID
import kotlin.test.*

@Suppress("ClassName")
open class DBTests_01_Batch : DBTests() {

    @Test
    fun test00_checkOK() {
        db.inflateDB()

        val uid = UUID.timeUUID()

        val counterKey = db.put(Primitive(Models.salomon.id, 0))

        val counter = db[counterKey]!!
        db.execBatch {
            put(Message(uid, db.keyFrom(Models.salomon), "Coucou !"))
            put(counter.copy(value = counter.value + 1))
            addOptions(Anticipate(true) { check(db[counterKey]!!.value == counter.value) })
        }

        assertEquals(1, db[counterKey]!!.value)
        assertNotNull(db[db.key<Message>(Value.of(uid))])
    }

    @Test
    fun test01_checkKO() {
        db.inflateDB()

        val uid = UUID.timeUUID()

        val counterKey = db.put(Primitive(Models.salomon.id, 0))

        val counter = db[counterKey]!!
        assertFailsWith<IllegalStateException> {
            db.execBatch {
                put(Message(uid, db.keyFrom(Models.salomon), "Coucou !"))
                put(counter.copy(value = counter.value + 1))
                addOptions(Anticipate(true) { check(db[counterKey]!!.value == counter.value + 1) })
            }
        }

        assertEquals(0, db[counterKey]!!.value)
        assertNull(db[db.key<Message>(Value.of(uid))])
    }

}
