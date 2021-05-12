package org.kodein.db.impl.model

import org.kodein.db.Middleware
import org.kodein.db.Value
import org.kodein.db.encryption.Encryption
import org.kodein.db.inDir
import org.kodein.db.inmemory.inMemory
import org.kodein.db.model.ModelDB
import org.kodein.db.model.findAllByIndex
import org.kodein.db.model.findAllByType
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.putAll
import org.kodein.memory.file.FileSystem
import org.kodein.memory.io.Memory
import org.kodein.memory.text.array
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class ModelDBTests_10_MetadataExtractor : ModelDBTests() {

    class LDB : ModelDBTests_10_MetadataExtractor(), ModelDBTests.LDB
    class IM : ModelDBTests_10_MetadataExtractor(), ModelDBTests.IM

    abstract class Encrypted : ModelDBTests_10_MetadataExtractor(), ModelDBTests.Encrypted {
        class LDB : Encrypted(), ModelDBTests.LDB
        class IM : Encrypted(), ModelDBTests.IM
    }


    override fun testMetadataExtractor() = MetadataExtractor { model, _ ->
        when (model) {
            is Date -> Metadata(
                    id = Value.of(model.year, model.month, model.day),
                    indexes = mapOf(
                            "month" to Value.of(model.month, model.year, model.day),
                            "day" to Value.of(model.day)
                    )
            )
            else -> throw UnsupportedOperationException()
        }
    }

    @Test
    fun test00_extractor() {
        val date1 = Date(15, 12, 1986)
        val date2 = Date(11, 12, 2019)

        mdb.putAll(listOf(date1, date2))

        mdb.findAllByType<Date>().use { cursor ->
            assertCursorIs(cursor) {
                K(date1) {
                    assertEquals(date1, it.model().model)
                }
                K(date2) {
                    assertEquals(date2, it.model().model)
                }
            }
        }

        mdb.findAllByIndex<Date>("month").use { cursor ->
            assertCursorIs(cursor) {
                K(date1) {
                    assertEquals(date1, it.model().model)
                }
                K(date2) {
                    assertEquals(date2, it.model().model)
                }
            }
        }

        mdb.findAllByIndex<Date>("day").use { cursor ->
            assertCursorIs(cursor) {
                K(date2) {
                    assertEquals(date2, it.model().model)
                }
                K(date1) {
                    assertEquals(date1, it.model().model)
                }
            }
        }
    }

}
