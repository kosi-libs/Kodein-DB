package org.kodein.db.impl.model.cache

import kotlinx.serialization.Serializable
import org.kodein.db.ValueConverter.Companion.key
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Date
import org.kodein.db.impl.model.default
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.cache.ModelCache
import org.kodein.db.model.get
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.file.FileSystem
import org.kodein.memory.use
import kotlin.test.*

@Suppress("ClassName")
abstract class CacheDBTests_05_Mutation : CacheDBTests() {

    class LDB : CacheDBTests_05_Mutation() { override val factory = ModelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : CacheDBTests_05_Mutation() { override val factory = ModelDB.inMemory }

    @Serializable
    data class T0(override val id: Int, var str: String) : Metadata

    @Test
    fun test00_mutationException() {
        val t = T0(0, "foo")
        val k = mdb.put(t).key
        t.str = "bar"
        assertFailsWith<ModelMutatedException> { mdb[k] }
    }

}
