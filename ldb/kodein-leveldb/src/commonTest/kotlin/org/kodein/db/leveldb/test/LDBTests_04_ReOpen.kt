package org.kodein.db.leveldb.test

import kotlinx.io.core.use
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LDBTests_04_ReOpen : LevelDBTests() {

    @Test
    fun test_00_PutCloseOpenGet() {
        ldb!!.put(buffer("key"), buffer("value"))

        ldb!!.close()

        ldb = factory.open("db", options().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        val value = ldb!!.get(buffer("key"))!!
        assertBytesEquals(byteArray("value"), value)
        value.close()
    }

    @Test
    fun test_01_PutCloseOpenIter() {
        ldb!!.put(buffer("key"), buffer("value"))

        ldb!!.close()

        ldb = factory.open("db", options().copy(openPolicy = LevelDB.OpenPolicy.OPEN))

        ldb!!.newCursor().use { cursor ->
            cursor.seekToFirst()

            assertTrue(cursor.isValid())
            assertBytesEquals(byteArray("key"), cursor.transientKey())
            assertBytesEquals(byteArray("value"), cursor.transientValue())

            cursor.next()
            assertFalse(cursor.isValid())
        }
    }

}
