package org.kodein.db.plugin.fts

import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.db.test.utils.newBuffer
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.native
import org.kodein.memory.util.deferScope
import kotlin.test.Test
import kotlin.test.assertEquals


class FtsKeysTests {

    @Test
    fun test00_indexKey() {
        deferScope {
            val documentKey = newBuffer('o', 0, 0, 0, 0, 1, "one", 0).useInScope()
            val size = ftsIndexKeySize("Salomon", "name", documentKey)
            assertEquals(29, size)
            val indexKey = Allocation.native(size) { putFtsIndexKey("Salomon", "name", documentKey) } .useInScope()
            assertBytesEquals(byteArray("XftsT", 0, "Salomon", 0, "name", 0, documentKey), indexKey)
        }
    }

    @Test
    fun test01_indexKeyPrefix() {
        deferScope {
            val size = ftsIndexKeyPrefixSize("Sa", true)
            assertEquals(8, size)
            val indexKeyPrefix = Allocation.native(size) { putFtsIndexKeyPrefix("Sa", true) } .useInScope()
            assertBytesEquals(byteArray("XftsT", 0, "Sa"), indexKeyPrefix)
        }
    }

    @Test
    fun test02_indexKeyGet() {
        deferScope {
            val documentKey = newBuffer('o', 0, 0, 0, 0, 42, "one", 0).useInScope()
            val indexKey = newBuffer("XftsT", 0, "Salomon", 0, "name", 0, documentKey).useInScope()
            assertEquals("Salomon", indexKey.getFtsIndexKeyToken())
            assertEquals("name", indexKey.getFtsIndexKeyField())
            assertBytesEquals(documentKey, indexKey.getFtsIndexKeyDocumentKey())
            assertEquals(42, indexKey.getFtsIndexKeyDocumentType())
        }
    }

    @Test
    fun test03_refKey() {
        deferScope {
            val documentKey = newBuffer('o', 0, 0, 0, 0, 1, "one", 0).useInScope()
            val size = ftsRefKeySize(documentKey, "name")
            assertEquals(20, size)
            val refKey = Allocation.native(size) { putFtsRefKey(documentKey, "name") } .useInScope()
            assertBytesEquals(byteArray("XftsR", 0, documentKey, "name"), refKey)
        }
    }

    @Test
    fun test04_refKeyPrefix() {
        deferScope {
            val documentKey = newBuffer('o', 0, 0, 0, 0, 1, "one", 0).useInScope()
            val size = ftsRefKeyPrefixSize(documentKey)
            assertEquals(16, size)
            val refKey = Allocation.native(size) { putFtsRefKeyPrefix(documentKey) } .useInScope()
            assertBytesEquals(byteArray("XftsR", 0, documentKey), refKey)
        }
    }

    @Test
    fun test05_refKeyGet() {
        deferScope {
            val documentKey = newBuffer('o', 0, 0, 0, 0, 42, "one", 0).useInScope()
            val refKey = newBuffer("XftsR", 0, documentKey, "name").useInScope()
            assertEquals("name", refKey.getFtsRefKeyField())
        }
    }

    @Test
    fun test06_refValue() {
        deferScope {
            val alloc = Allocation.native(16) {
                putFtsRefValueToken("word")
                putFtsRefValueToken("слово")
            }.useInScope()
            assertBytesEquals(byteArray("word", 0, "слово", 0), alloc)
        }
    }

    @Test
    fun test07_refValueGet() {
        deferScope {
            val refValue = newBuffer("word", 0, "слово", 0).useInScope()
            assertEquals(listOf("word", "слово"), refValue.getFtsRefValueTokens().toList())
        }
    }

}
