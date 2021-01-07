package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.file.FileSystem
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class DataDBTests_02_Get : DataDBTests() {

    class LDB : DataDBTests_02_Get() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_02_Get() { override val factory = DataDB.inMemory }


    @Test
    fun test00_GetExisting() {
        val aKey = ddb.newKey(1, Value.ofAscii("aaa"))
        ddb.put(aKey, Value.ofAscii("ValueA1!"), mapOf("Symbols" to Value.ofAscii("alpha", "beta")))
        val bKey = ddb.newKey(1, Value.ofAscii("bbb"))
        ddb.put(bKey, Value.ofAscii("ValueB1!"), mapOf("Numbers" to Value.ofAscii("forty", "two")))

        assertDataIs(byteArray("ValueA1!"), ddb.get(aKey))
        assertDataIs(byteArray("ValueB1!"), ddb.get(bKey))
    }

    @Test
    fun test01_GetUnknownInEmptyDB() {
        val key = ddb.newKey(1, Value.ofAscii("aaa"))
        assertNull(ddb.get(key))
    }

    @Test
    fun test02_GetUnknownInNonEmptyDB() {
        ddb.put(ddb.newKey(1, Value.ofAscii("aaa")), Value.ofAscii("ValueA1!"), mapOf("Symbols" to Value.ofAscii("alpha", "beta")))

        assertNull(ddb.get(ddb.newKey(1, Value.ofAscii("bbb"))))
    }

}
