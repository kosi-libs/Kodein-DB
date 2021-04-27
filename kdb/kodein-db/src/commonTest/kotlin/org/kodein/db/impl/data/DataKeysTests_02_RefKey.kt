package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.array
import org.kodein.db.test.utils.int
import org.kodein.db.test.utils.ushort
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.asMemory
import org.kodein.memory.io.native
import org.kodein.memory.util.deferScope
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class DataKeysTests_02_RefKey {

    @Test
    fun test00_RefBody() {
        val indexes = mapOf(
            "Symbols" to listOf(
                Value.of("alpha", "beta") to Value.of("SymbolsMetadata1"),
                Value.of("delta", "gamma") to Value.of("SymbolsMetadata2")
            ),
            "Numbers" to listOf(Value.of("forty", "two") to null)
        )
        val size = getRefBodySize(indexes)
        assertEquals(61, size)

        deferScope {
            val refBody = Allocation.native(128) { writeRefBody(indexes) } .useInScope()
            assertBytesEquals(array(128, "Symbols", 0, int(25), ushort(10), "alpha", 0, "beta", ushort(11), "delta", 0, "gamma", "Numbers", 0, int(11), ushort(9), "forty", 0, "two"), refBody)
        }
    }

    @Test
    fun test01_RefBodyIndexNames() {
        val indexes = mapOf(
            "Symbols" to listOf(
                Value.of("alpha", "beta") to Value.of("SymbolsMetadata1"),
                Value.of("delta", "gamma") to Value.of("SymbolsMetadata2")
            ),
            "Numbers" to listOf(Value.of("forty", "two") to null)
        )
        deferScope {
            val refBody = Allocation.native(61) { writeRefBody(indexes) } .useInScope()
            assertEquals(setOf("Symbols", "Numbers"), getRefBodyIndexNames(refBody))
        }
    }

    @Test
    fun test02_RefBodyIndexKeys() {
        val indexes = mapOf(
            "Symbols" to listOf(
                Value.of("alpha", "beta") to Value.of("SymbolsMetadata1"),
                Value.of("delta", "gamma") to Value.of("SymbolsMetadata2")
            ),
            "Numbers" to listOf(Value.of("forty", "two") to null)
        )
        deferScope {
            val docKey = Allocation.native(32) { writeDocumentKey(1, Value.of("aaa")) }
            val refKey = Allocation.native(32) { writeRefKeyFromDocumentKey(docKey) }
            val refBody = Allocation.native(61) { writeRefBody(indexes) } .useInScope()

            val expected = listOf(
                array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0),
                array('i', 0, int(1), "Symbols", 0, "delta", 0, "gamma", 0, "aaa", 0),
                array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0)
            )

            getRefIndexKeys(refKey, refBody).forEachIndexed { i, actual ->
                assertBytesEquals(expected[i], actual)
            }
        }
    }

    @Test
    fun test03_RefBodyV0IndexNames() {
        val refBody = array(
            int(29), 'i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0,
            int(28), 'i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0
        ).asMemory()
        assertEquals(setOf("Symbols", "Numbers"), getRefBodyIndexNames(refBody))
    }

    @Test
    fun test04_RefV0IndexKeys() {
        val refKey = array('r', 0, int(1), "aaa", 0).asMemory()
        val refBody = array(
            int(29), 'i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0,
            int(28), 'i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0
        ).asMemory()

        val expected = listOf(
            array('i', 0, int(1), "Symbols", 0, "alpha", 0, "beta", 0, "aaa", 0),
            array('i', 0, int(1), "Numbers", 0, "forty", 0, "two", 0, "aaa", 0)
        )
        getRefIndexKeys(refKey, refBody).forEachIndexed { i, actual ->
            assertBytesEquals(expected[i], actual)
        }
    }
}
