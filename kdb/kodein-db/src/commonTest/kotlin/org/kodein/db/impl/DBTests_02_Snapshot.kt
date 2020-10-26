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

    class LDB : DBTests_02_Snapshot() { override val factory = DB.inDir(FileSystem.tempDirectory.path) }
    class IM : DBTests_02_Snapshot() { override val factory = DB.inMemory }

    abstract class NoCache : DBTests_02_Snapshot() {
        override fun options(): Array<out Options.Open> = arrayOf(kxSerializer, ModelCache.Disable)
        class LDB : NoCache() { override val factory = DB.inDir(FileSystem.tempDirectory.path) }
        class IM : NoCache() { override val factory = DB.inMemory }
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
