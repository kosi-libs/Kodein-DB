package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Birth
import org.kodein.db.impl.model.City
import org.kodein.db.impl.model.Message
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.Primitive
import org.kodein.db.model.cache.ModelCache
import org.kodein.memory.file.FileSystem
import org.kodein.memory.util.UUID
import kotlin.test.*

@Suppress("ClassName")
abstract class DBTests_01_Batch : DBTests() {

    class LDB : DBTests_01_Batch() { override val factory = DB.inDir(FileSystem.tempDirectory.path) }
    class IM : DBTests_01_Batch() { override val factory = DB.inMemory }

    abstract class NoCache : DBTests_01_Batch() {
        override fun options(): Array<out Options.Open> = arrayOf(kxSerializer, ModelCache.Disable)
        class LDB : NoCache() { override val factory = DB.inDir(FileSystem.tempDirectory.path) }
        class IM : NoCache() { override val factory = DB.inMemory }
    }


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
