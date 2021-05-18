package org.kodein.db.encryption

import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataDB
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.assertDBIs
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import kotlin.test.AfterTest
import kotlin.test.BeforeTest


abstract class EncryptionTests {

    private var _ddb: DataDB? = null
    protected val ddb: DataDB get() = _ddb!!

    protected fun open() {
        _ddb = EncryptedDataDB(DataDB.inMemory.open("testdb"), EncryptOptions.Encrypt(Memory.array("Master-Key"))) {
            when (it) {
                2 -> EncryptOptions.KeepPlain
                3 -> EncryptOptions.Encrypt(Memory.array("Key3"), false, EncryptOptions.Indexes.None)
                4 -> EncryptOptions.Encrypt(Memory.array("Key4"), true, EncryptOptions.Indexes.Only("Numbers"))
                else -> null
            }
        }
    }

    @BeforeTest
    open fun setUp() {
        DataDB.inMemory.destroy("testdb")
        open()
    }

    @AfterTest
    open fun tearDown() {
        _ddb?.close()
        _ddb = null
        DataDB.inMemory.destroy("testdb")
    }

    fun assertCursorIs(key: ByteArray, value: ByteArray, it: DataCursor) {
        assertBytesEquals(key, it.transientKey())
        assertBytesEquals(value, it.transientValue())
    }

    fun assertDBIs(vararg keyValues: Pair<ByteArray, ByteArray>) {
        assertDBIs(ddb.kv.ldb, *keyValues)
    }
}
