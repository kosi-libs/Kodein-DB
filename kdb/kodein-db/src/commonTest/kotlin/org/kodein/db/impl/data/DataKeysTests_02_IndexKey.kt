package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.native
import org.kodein.memory.use
import org.kodein.memory.util.deferScope
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class DataKeysTests_02_IndexKey {

    @Test
    fun test00_SimpleIndexKey() {
        deferScope {
            val objectKey = Allocation.native(32) {
                writeDocumentKey(1, Value.of("one"))
            }.useInScope()

            val indexSize = getIndexKeySize(objectKey, "Symbols", Value.of("alpha"))
            assertEquals(24, indexSize)

            val indexKey = Allocation.native(indexSize) {
                writeIndexKey(objectKey, "Symbols", Value.of("alpha"))
            }.useInScope()
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "one", 0), indexKey)
        }
    }

    @Test
    fun test01_CompositeIndexKey() {
        deferScope {
            val objectKey = Allocation.native(32) {
                writeDocumentKey(1, Value.of("one", "two"))
            }.useInScope()

            val indexSize = getIndexKeySize(objectKey, "Symbols", Value.of("alpha", "beta"))
            assertEquals(33, indexSize)
            val indexKey = Allocation.native(indexSize) {
                writeIndexKey(objectKey, "Symbols", Value.of("alpha", "beta"))
            }.useInScope()
            assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "one", 0, "two", 0), indexKey)
        }
    }

}
