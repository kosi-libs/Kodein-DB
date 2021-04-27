package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.array
import org.kodein.memory.file.FileSystem
import kotlin.test.Test
import kotlin.test.assertNull

@Suppress("ClassName")
abstract class DataDBTests_02_Get : DataDBTests() {

    class LDB : DataDBTests_02_Get() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_02_Get() { override val factory = DataDB.inMemory }


    @Test
    fun test00_GetExisting() {
        val aKey = ddb.newKey(1, Value.of("aaa"))
        ddb.put(aKey, Value.of("ValueA1!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))
        val bKey = ddb.newKey(1, Value.of("bbb"))
        ddb.put(bKey, Value.of("ValueB1!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null)))

        assertDataIs(array("ValueA1!"), ddb.get(aKey))
        assertDataIs(array("ValueB1!"), ddb.get(bKey))
    }

    @Test
    fun test01_GetUnknownInEmptyDB() {
        val key = ddb.newKey(1, Value.of("aaa"))
        assertNull(ddb.get(key))
    }

    @Test
    fun test02_GetUnknownInNonEmptyDB() {
        ddb.put(ddb.newKey(1, Value.of("aaa")), Value.of("ValueA1!"), mapOf("Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))

        assertNull(ddb.get(ddb.newKey(1, Value.of("bbb"))))
    }

}
