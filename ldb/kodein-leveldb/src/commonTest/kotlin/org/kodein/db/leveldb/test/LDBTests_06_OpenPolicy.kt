package org.kodein.db.leveldb.test

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBException
import kotlin.test.Test
import kotlin.test.assertFailsWith

@Suppress("ClassName")
class LDBTests_06_OpenPolicy : LevelDBTests() {

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
