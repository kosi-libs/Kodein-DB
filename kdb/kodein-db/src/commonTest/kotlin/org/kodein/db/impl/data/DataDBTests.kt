package org.kodein.db.impl.data

import org.kodein.db.DBFactory
import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataDB
import org.kodein.db.kv.DBLoggerFactory
import org.kodein.db.kv.FailOnBadClose
import org.kodein.db.kv.TrackClosableAllocation
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.assertDBIs
import org.kodein.log.LoggerFactory
import org.kodein.log.frontend.printFrontend
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class DataDBTests {

    private var _ddb: DataDB? = null

    protected val ddb: DataDB get() = _ddb!!

    abstract val factory: DBFactory<DataDB>

    protected fun open() {
        _ddb = factory.open("datadb", TrackClosableAllocation(true), FailOnBadClose(true), DBLoggerFactory(LoggerFactory(printFrontend)))
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
        assertDBIs(ddb.kv.ldb, *keyValues)
    }

}
