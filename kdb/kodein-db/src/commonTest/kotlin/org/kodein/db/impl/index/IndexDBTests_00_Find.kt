package org.kodein.db.impl.index

import org.kodein.db.impl.DBTests
import org.kodein.db.index.*
import kotlin.test.*

@Suppress("ClassName")
abstract class IndexDBTests_00_Find : IndexDBTests() {
    class LDB : IndexDBTests_00_Find(), DBTests.LDB
    class IM : IndexDBTests_00_Find(), DBTests.IM

    @Test
    fun test00_anyAndNoneFilter() {
        db.inflateIndexDB()

        assertTrue(db.any(IndexCity.Indexes.name eq "Berlin"))
        assertTrue(db.none(IndexCity.Indexes.name eq "Kopenhagen"))
    }

    @Test
    fun test01_findModelListFilter() {
        db.inflateIndexDB()

        assertEquals(listOf(IndexModels.dresden, IndexModels.berlin), db.findModelList(IndexCity.Indexes.country eq "Germany"))
        assertEquals(emptyList(), db.findModelList(IndexCity.Indexes.country eq "Ger"))

        assertEquals(listOf(IndexModels.dresden, IndexModels.berlin), db.findModelList(IndexCity.Indexes.country sw "Ger"))
    }

    @Test
    fun test02_findFilter_overTriple() {
        db.inflateIndexDB()

        assertEquals(IndexModels.paris, db.findOneOrNull(IndexCity.Indexes.nameCountryPostalCode eq Triple("Paris", "France", 75000)))

        assertFailsWith<NoSuchElementException> { db.findOne(IndexCity.Indexes.nameCountryPostalCode eq Triple("Paris", "Texas", 75460)) }
        assertNull(db.findOneOrNull(IndexCity.Indexes.nameCountryPostalCode eq Triple("Paris", "Texas", 75460)))
    }

    @Test
    fun test03_findAllByIndex() {
        db.inflateIndexDB()

        assertEquals(listOf(IndexModels.berlin, IndexModels.dresden, IndexModels.paris), db.findModelList(+IndexCity.Indexes.name))
    }
}
