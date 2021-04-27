package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataDB
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.db.test.utils.ushort
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.asMemory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class DataDBTests_11_FindIndexes : DataDBTests() {

    class LDB : DataDBTests_11_FindIndexes() { override val factory = DataDB.default.inDir(FileSystem.tempDirectory.path) }
    class IM : DataDBTests_11_FindIndexes() { override val factory = DataDB.inMemory }


    @Test
    fun test00_FindIndexes() {
        val key = ddb.newKey(1, Value.of("aaa"))
        ddb.put(key, Value.of("ValueA!"), mapOf("Numbers" to listOf(Value.of("forty", "two") to null), "Symbols" to listOf(Value.of("alpha", "beta") to Value.of("MetaSymbolsA"))))

        assertEquals(setOf("Numbers", "Symbols"), ddb.getIndexesOf(key))
    }

    @Test
    fun test01_FindNoIndexes() {
        val key = ddb.newKey(1, Value.of("aaa"))
        ddb.put(key, Value.of("ValueA!"))
        val indexes = ddb.getIndexesOf(key)
        assertTrue(indexes.isEmpty())
    }

    @Test
    fun test02_FindUnknownIndexes() {
        val indexes = ddb.getIndexesOf(ddb.newKey(2, Value.of("A")))

        assertTrue(indexes.isEmpty())
    }
}
