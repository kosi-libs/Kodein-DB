package org.kodein.db.impl.model

import org.kodein.db.Anticipate
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.model.*
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import kotlin.test.*

@Suppress("ClassName")
open class ModelDBTests_11_MetadataExtractor : ModelDBTests() {

    override fun testMetadataExtractor() = MetadataExtractor {
        when (it) {
            is Date -> Metadata(
                    id = Value.of(it.year, it.month, it.day),
                    indexes = indexSet(
                            "month" to Value.of(it.month, it.year, it.day),
                            "day" to Value.of(it.day)
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

        mdb.findAllByType<Date>().use {
            assertTrue(it.isValid())
            assertEquals(date1, it.model().model)
            it.next()
            assertTrue(it.isValid())
            assertEquals(date2, it.model().model)
            it.next()
            assertFalse(it.isValid())
        }

        mdb.findAllByIndex<Date>("month").use {
            assertTrue(it.isValid())
            assertEquals(date1, it.model().model)
            it.next()
            assertTrue(it.isValid())
            assertEquals(date2, it.model().model)
            it.next()
            assertFalse(it.isValid())
        }

        mdb.findAllByIndex<Date>("day").use {
            assertTrue(it.isValid())
            assertEquals(date2, it.model().model)
            it.next()
            assertTrue(it.isValid())
            assertEquals(date1, it.model().model)
            it.next()
            assertFalse(it.isValid())
        }
    }

}
