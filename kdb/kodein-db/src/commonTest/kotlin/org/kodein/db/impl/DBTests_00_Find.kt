package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.encryption.DBFeatureDisabledError
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Birth
import org.kodein.db.impl.model.City
import org.kodein.db.impl.model.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Suppress("ClassName")
abstract class DBTests_00_Find : DBTests() {

    class LDB : DBTests_00_Find(), DBTests.LDB
    class IM : DBTests_00_Find(), DBTests.IM

    abstract class NoCache : DBTests_00_Find(), DBTests.NoCache {
        class LDB : NoCache(), DBTests.LDB
        class IM : NoCache(), DBTests.IM
    }

    abstract class Encrypted : DBTests_00_Find(), DBTests.Encrypted {
        @Test override fun test02_findById() { assertFailsWith<DBFeatureDisabledError> { super.test02_findById() } }
        @Test override fun test03_findByIdOpen() { assertFailsWith<DBFeatureDisabledError> { super.test03_findByIdOpen() } }
        @Test override fun test06_findByIndexOpen()  { assertFailsWith<DBFeatureDisabledError> { super.test06_findByIndexOpen() } }

        class LDB : Encrypted(), DBTests.LDB
        class IM : Encrypted(), DBTests.IM

        abstract class NoCache : Encrypted(), DBTests.NoCache {
            class LDB : NoCache(), DBTests.LDB
            class IM : NoCache(), DBTests.IM
        }
    }

    override fun testOptions(): Array<out Options.Open> = (super.testOptions().filterNot { it is TypeTable } + TypeTable {
        root<Adult>()
        root<City>()
        root<Birth>()
    }).toTypedArray()

    @Test
    fun test00_findAll() {
        db.inflateDB()

        val all = db.findAll().useModels { it.toList() }
        assertListEquals(listOf(Models.sjeg, Models.paris, Models.pap, Models.salomon, Models.laila, Models.salomonBirth(db), Models.lailaBirth(db)), all.toList())
    }

    @Test
    fun test01_findAllByType() {
        db.inflateDB()

        assertListEquals(listOf(Models.salomon, Models.laila), db.find<Adult>().all().useModels { it.toList() })
        assertListEquals(listOf(Models.salomonBirth(db), Models.lailaBirth(db)), db.find<Birth>().all().useModels { it.toList() })
        assertListEquals(listOf(Models.sjeg, Models.paris, Models.pap), db.find<City>().all().useModels { it.toList() })
    }

    @Test
    open fun test02_findById() {
        db.inflateDB()

        assertListEquals(listOf(Models.salomon), db.find<Adult>().byId("BRYS").useModels { it.toList() })
    }

    @Test
    open fun test03_findByIdOpen() {
        db.inflateDB()

        assertListEquals(listOf(Models.salomon, Models.laila), db.find<Adult>().byId("BRYS", isOpen = true).useModels { it.toList() })
    }

    @Test
    fun test04_findAllByIndex() {
        db.inflateDB()

        assertListEquals(listOf(Models.paris, Models.pap, Models.sjeg), db.find<City>().byIndex("name").useModels { it.toList() })
    }

    @Test
    fun test05_findByIndex() {
        db.inflateDB()

        assertListEquals(listOf(Models.paris), db.find<City>().byIndex("name", "Paris").useModels { it.toList() })
    }

    @Test
    open fun test06_findByIndexOpen() {
        db.inflateDB()

        assertListEquals(listOf(Models.paris, Models.pap), db.find<City>().byIndex("name", "P", isOpen = true).useModels { it.toList() })
    }

    @Test
    fun test07_getIndexes() {
        db.inflateDB()

        assertEquals(setOf("firstName", "birth"), db.getIndexesOf(db.keyFrom(Models.salomon)))
    }

    @Test
    fun test08_entries() {
        db.inflateDB()

        val all = db.findAll().useEntries { it.toList() }

        assertEquals<Map<Key<Any>, Any>>(
            hashMapOf(
                db.keyFrom(Models.sjeg) to Models.sjeg,
                db.keyFrom(Models.paris) to Models.paris,
                db.keyFrom(Models.pap) to Models.pap,
                db.keyFrom(Models.laila) to Models.laila,
                db.keyFrom(Models.salomon) to Models.salomon,
                db.keyFrom(Models.lailaBirth(db)) to Models.lailaBirth(db),
                db.keyFrom(Models.salomonBirth(db)) to Models.salomonBirth(db)
            ),
            all.associate { it.key to it.model }
        )
    }

    @Test
    fun test09_findByCompositeIndex() {
        db.inflateDB()
        val kit = Adult("Kit", "Harington", Date(26, 12, 1986))
        db.put(kit)
        db.put(Adult("Robert", "Pattinson", Date(13, 5, 1986)))

        val list = db.find<Adult>().byIndex("birth", 1986, 12).useModels { it.toList() }
        if (this !is Encrypted) {
            assertListEquals(listOf(Models.salomon, kit), list)
        } else {
            assertTrue(list.isEmpty()) // Encryption disables support for non-complete composite key search
        }
    }

}
