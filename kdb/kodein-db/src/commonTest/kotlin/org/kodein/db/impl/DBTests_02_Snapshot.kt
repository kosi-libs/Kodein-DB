package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.Options
import org.kodein.db.delete
import org.kodein.db.get
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.cache.ModelCache
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class DBTests_02_Snapshot : DBTests() {

    class LDB : DBTests_02_Snapshot(), DBTests.LDB
    class IM : DBTests_02_Snapshot(), DBTests.IM

    abstract class NoCache : DBTests_02_Snapshot(), DBTests.NoCache {
        class LDB : NoCache(), DBTests.LDB
        class IM : NoCache(), DBTests.IM
    }

    abstract class Encrypted : DBTests_02_Snapshot(), DBTests.Encrypted {
        class LDB : Encrypted(), DBTests.LDB
        class IM : Encrypted(), DBTests.IM

        abstract class NoCache : Encrypted(), DBTests.NoCache {
            class LDB : NoCache(), DBTests.LDB
            class IM : NoCache(), DBTests.IM
        }
    }


    @Test
    fun test00_snapshot() {
        db.inflateDB()

        val me = db.keyFrom(Models.salomon)

        db.newSnapshot().use { snapshot ->
            db.delete(me)
            assertNull(db[me])
            assertNotNull(snapshot[me])
        }
    }


}
