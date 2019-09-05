package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.description
import org.kodein.db.test.utils.platformTmpPath
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.readBytes
import org.kodein.memory.use
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull
import kotlin.test.fail

abstract class DataDBTests {

    private var _ddb: DataDB? = null

    protected val ddb: DataDB get() = _ddb!!

    private val factory = DataDB.default.inDir(platformTmpPath)

    protected fun open() {
        _ddb = factory.open("datadb")
    }

    @BeforeTest
    fun setUp() {
        factory.destroy("datadb")
        open()
    }

    @AfterTest
    fun tearDown() {
        _ddb?.close()
        _ddb = null
        factory.destroy("datadb")
    }

    fun assertCursorIs(key: ByteArray, value: ByteArray, it: DataCursor) {
        assertBytesEquals(key, it.transientKey().bytes)
        assertBytesEquals(value, it.transientValue().bytes)
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