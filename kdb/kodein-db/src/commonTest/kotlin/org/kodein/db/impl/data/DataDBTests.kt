package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.ldb.DBLoggerFactory
import org.kodein.db.ldb.FailOnBadClose
import org.kodein.db.ldb.TrackClosableAllocation
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.description
import org.kodein.log.LoggerFactory
import org.kodein.log.frontend.printFrontend
import org.kodein.memory.file.FileSystem
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

    private val factory = DataDB.default.inDir(FileSystem.tempDirectory.path)

    protected fun open() {
        _ddb = factory.open("datadb", TrackClosableAllocation(true), FailOnBadClose(true), DBLoggerFactory(LoggerFactory(listOf(printFrontend))))
    }

    @BeforeTest
    fun setUp() {
        _ddb?.close()
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
                assertBytesEquals(keyValues[i].first, cursor.transientKey(), prefix = "Key ${i + 1}: ")
                assertBytesEquals(keyValues[i].second, cursor.transientValue(), prefix = "Value ${i + 1}: ")
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