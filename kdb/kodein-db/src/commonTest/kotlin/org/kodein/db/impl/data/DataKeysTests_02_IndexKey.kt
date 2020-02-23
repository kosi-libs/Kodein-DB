package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.native
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class DataKeysTests_02_IndexKey {

    @Test
    fun test00_SimpleIndexKey() {
        Allocation.native(32).use { objectKey ->
            objectKey.putDocumentKey(1, Value.ofAscii("one"))
            objectKey.flip()

            val indexSize = getIndexKeySize(objectKey, "Symbols", Value.ofAscii("alpha"))
            assertEquals(24, indexSize)
            Allocation.native(indexSize).use { indexKey ->
                indexKey.putIndexKey(objectKey, "Symbols", Value.ofAscii("alpha"))
                indexKey.flip()
                assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "one", 0), indexKey)
            }
        }
    }

    @Test
    fun test01_CompositeIndexKey() {
        Allocation.native(32).use { objectKey ->
            objectKey.putDocumentKey(1, Value.ofAscii("one", "two"))
            objectKey.flip()

            val indexSize = getIndexKeySize(objectKey, "Symbols", Value.ofAscii("alpha", "beta"))
            assertEquals(33, indexSize)
            Allocation.native(indexSize).use { indexKey ->
                indexKey.putIndexKey(objectKey, "Symbols", Value.ofAscii("alpha", "beta"))
                indexKey.flip()
                assertBytesEquals(byteArray('i', 0, 0, 0, 0, 1, "Symbols", 0, "alpha", 0, "beta", 0, "one", 0, "two", 0), indexKey)
            }
        }
    }

}
