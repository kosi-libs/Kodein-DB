package org.kodein.db.impl

import org.kodein.db.Value
import org.kodein.db.find
import org.kodein.db.get
import org.kodein.db.impl.model.*
import org.kodein.db.models
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
class DBTests_00_Find : DBTests() {

    @Test
    fun test00_findAll() {
        inflateDB()

        val all = db.findAll().models().toList()
        assertEquals(7, all.size)
        assertEquals(listOf(Models.salomon, Models.laila), all.subList(0, 2))
        assertEquals(listOf("Salomon BRYS: Saint Julien En Genevois", "Laila BRYS-ATIE: Pointe À Pitre"), all.subList(2, 4).map { it as Birth } .map { db[it.adult]!!.fullName + ": " + db[it.city]!!.name } .toList())
        assertEquals(listOf(Models.sjeg, Models.paris, Models.pap), all.subList(4, 7).toList())
    }

    @Test
    fun test01_findAllByType() {
        inflateDB()

        assertEquals(listOf(Models.salomon, Models.laila), db.find<Adult>().all().models().toList())
        assertEquals(listOf("Salomon BRYS: Saint Julien En Genevois", "Laila BRYS-ATIE: Pointe À Pitre"), db.find<Birth>().all().models() .map { db[it.adult]!!.fullName + ": " + db[it.city]!!.name } .toList())
        assertEquals(listOf(Models.sjeg, Models.paris, Models.pap), db.find<City>().all().models().toList())
    }

    @Test
    fun test02_findById() {
        inflateDB()

        assertEquals(listOf(Models.salomon), db.find<Adult>().byId().withValue(Value.ofAscii("BRYS")).models().toList())
    }

    @Test
    fun test03_findByIdOpen() {
        inflateDB()

        assertEquals(listOf(Models.salomon, Models.laila), db.find<Adult>().byId().withValue(Value.ofAscii("BRYS"), isOpen = true).models().toList())
    }

    @Test
    fun test04_findAllByIndex() {
        inflateDB()

        assertEquals(listOf(Models.paris, Models.pap, Models.sjeg), db.find<City>().byIndex("name").all().models().toList())
    }

    @Test
    fun test05_findByIndex() {
        inflateDB()

        assertEquals(listOf(Models.paris), db.find<City>().byIndex("name").withValue(Value.ofAscii("Paris")).models().toList())
    }

    @Test
    fun test06_findByIndexOpen() {
        inflateDB()

        assertEquals(listOf(Models.paris, Models.pap), db.find<City>().byIndex("name").withValue(Value.ofAscii("P"), isOpen = true).models().toList())
    }

    @Test
    fun test07_getIndexes() {
        inflateDB()

        assertEquals(listOf("firstName", "birth"), db.getIndexesOf(db.newKey(Models.salomon)))
    }

}
