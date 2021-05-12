package org.kodein.db.impl

import kotlinx.serialization.Serializable
import org.kodein.db.AnticipateInLock
import org.kodein.db.execBatch
import org.kodein.db.get
import org.kodein.db.getById
import org.kodein.db.impl.model.Message
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.util.UUID
import kotlin.test.*

@Suppress("ClassName")
abstract class DBTests_01_Batch : DBTests() {

    class LDB : DBTests_01_Batch(), DBTests.LDB
    class IM : DBTests_01_Batch(), DBTests.IM

    abstract class NoCache : DBTests_01_Batch(), DBTests.NoCache {
        class LDB : NoCache(), DBTests.LDB
        class IM : NoCache(), DBTests.IM
    }

    abstract class Encrypted : DBTests_01_Batch(), DBTests.Encrypted {
        class LDB : Encrypted(), DBTests.LDB
        class IM : Encrypted(), DBTests.IM

        abstract class NoCache : Encrypted(), DBTests.NoCache {
            class LDB : NoCache(), DBTests.LDB
            class IM : NoCache(), DBTests.IM
        }
    }

    @Serializable
    private data class Counter(override val id: List<String>, val count: Int) : Metadata

    @Test
    fun test00_checkOK() {
        db.inflateDB()

        val uid = UUID.timeUUID()

        val counterKey = db.put(Counter(Models.salomon.id, 0))

        val counter = db[counterKey]!!
        db.execBatch {
            put(Message(uid, db.keyFrom(Models.salomon), "Coucou !"))
            put(counter.copy(count = counter.count + 1))
            addOptions(AnticipateInLock { check(db[counterKey]!!.count == counter.count) })
        }

        assertEquals(1, db[counterKey]!!.count)
        assertNotNull(db.getById<Message>(uid))
    }

    @Test
    fun test01_checkKO() {
        db.inflateDB()

        val uid = UUID.timeUUID()

        val counterKey = db.put(Counter(Models.salomon.id, 0))

        val counter = db[counterKey]!!
        assertFailsWith<IllegalStateException> {
            db.execBatch {
                put(Message(uid, db.keyFrom(Models.salomon), "Coucou !"))
                put(counter.copy(count = counter.count + 1))
                addOptions(AnticipateInLock { check(db[counterKey]!!.count == counter.count + 1) })
            }
        }

        assertEquals(0, db[counterKey]!!.count)
        assertNull(db.getById<Message>(uid))
    }

}
