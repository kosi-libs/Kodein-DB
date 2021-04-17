package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.test.utils.assertBytesEquals
import org.kodein.db.test.utils.byteArray
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.native
import org.kodein.memory.use
import org.kodein.memory.util.deferScope
import kotlin.test.Test

@Suppress("ClassName")
class DataKeysTests_04_IndexKeyInfos {

    @Test
    fun test00_IndexName() {
        deferScope {
            val objectKey = Allocation.native(32) {
                writeDocumentKey(1, Value.of("one"))
            }.useInScope()

            val indexKey = Allocation.native(32) {
                writeIndexKey(objectKey, "Symbols", Value.of("alpha", "beta"))
            }.useInScope()
            assertBytesEquals(byteArray("Symbols"), getIndexKeyName(indexKey))
        }
    }
}
