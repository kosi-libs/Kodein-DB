package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.*
import org.kodein.db.leveldb.inmemory.inMemory
import org.kodein.memory.file.FileSystem
import kotlin.test.Test
import kotlin.test.assertFailsWith

@Suppress("ClassName")
abstract class LDBTests_06_OpenPolicy : LevelDBTests() {

    class LDB : LDBTests_06_OpenPolicy() { override val factory: LevelDBFactory = LevelDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : LDBTests_06_OpenPolicy() { override val factory: LevelDBFactory = LevelDB.inMemory }


    @Test
    fun test_00_OpenInexisting() {
        assertFailsWith<LevelDBException> {
            try {
                factory.open("none", options().copy(openPolicy = LevelDB.OpenPolicy.OPEN))
            } finally {
                factory.destroy("none")
            }
        }
    }

    @Test
    fun test_01_ForceCreate() {
        try {
            factory.open("new", options().copy(openPolicy = LevelDB.OpenPolicy.CREATE)).close()
        } finally {
            factory.destroy("new")
        }
    }

    @Test
    fun test_02_ForceCreateExisting() {
        assertFailsWith<LevelDBException> {
            factory.open("db", options().copy(openPolicy = LevelDB.OpenPolicy.CREATE))
        }
    }

}
