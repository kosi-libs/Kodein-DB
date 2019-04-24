package org.kodein.db.impl.data

import org.kodein.db.Index
import org.kodein.db.Value
import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataDB
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.description
import org.kodein.memory.Allocation
import org.kodein.memory.readBytes
import org.kodein.memory.use
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull
import kotlin.test.fail

expect object DataDBTestFactory {
    fun destroy()
    fun open(): DataDB
}

abstract class DataDBTests {

    protected var _ddb: DataDB? = null

    protected val ddb: DataDB get() = _ddb!!

    protected val _factory = DataDBTestFactory

    @BeforeTest
    fun setUp() {
        _factory.destroy()
        _ddb = _factory.open()
    }

    @AfterTest
    fun tearDown() {
        ddb.close()
        _ddb = null
        _factory.destroy()
    }

    fun indexSet(vararg nameValues: Pair<String, Value>): Set<Index> =
            nameValues.map { Index(it.first, it.second) } .toHashSet()

    fun assertCursorIs(key: ByteArray, value: ByteArray, it: DataCursor) {
        assertBytesEquals(key, it.transientKey())
        assertBytesEquals(value, it.transientValue())
    }

    fun assertDBIs(vararg keyValues: Pair<ByteArray, ByteArray>) {
        (ddb as DataDBImpl).ldb.newCursor().use { cursor ->
            cursor.seekToFirst()
            var i = 0
            while (cursor.isValid()) {
                if (i >= keyValues.size) {
                    fail("DB contains additional entrie(s): " + cursor.transientKey().readBytes().description())
                }
                assertBytesEquals(keyValues[i].first, cursor.transientKey())
                assertBytesEquals(keyValues[i].second, cursor.transientValue())
                cursor.next()
                i++
            }
            if (i < keyValues.size) {
                fail("DB is missing entrie(s):\n" + keyValues.takeLast(keyValues.size - i).joinToString("\n") { it.first.description() + ": " + it.second.description() })
            }
        }
    }

    fun assertDataIs(expectedBytes: ByteArray, actual: Allocation?) {
        assertNotNull(actual)
        actual.use {
            assertBytesEquals(expectedBytes, actual)
        }
    }

}