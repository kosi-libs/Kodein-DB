package org.kodein.db.impl.model

import org.kodein.db.Value
import org.kodein.db.indexSet
import org.kodein.db.model.findAllByIndex
import org.kodein.db.model.findAllByType
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.putAll
import org.kodein.memory.use
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ClassName")
open class ModelDBTests_10_MetadataExtractor : ModelDBTests() {

    override fun testMetadataExtractor() = MetadataExtractor { model, _ ->
        when (model) {
            is Date -> Metadata(
                    id = Value.of(model.year, model.month, model.day),
                    indexes = indexSet(
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
