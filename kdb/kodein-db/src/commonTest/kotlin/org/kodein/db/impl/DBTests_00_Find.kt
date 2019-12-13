package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.impl.model.*
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("ClassName")
open class DBTests_00_Find : DBTests() {

    @Test
    fun test00_findAll() {
        db.inflateDB()

        val all = db.findAll().models().toList()
        assertEquals(7, all.size)
        assertEquals(listOf(Models.salomon, Models.laila), all.subList(0, 2))
        assertEquals(listOf("Salomon BRYS: Saint Julien En Genevois", "Laila BRYS-ATIE: Pointe À Pitre"), all.subList(2, 4).map { it as Birth } .map { db[it.adult]!!.fullName + ": " + db[it.city]!!.name } .toList())
        assertEquals(listOf(Models.sjeg, Models.paris, Models.pap), all.subList(4, 7).toList())
    }

    @Test
    fun test01_findAllByType() {
        db.inflateDB()

        assertEquals(listOf(Models.salomon, Models.laila), db.find<Adult>().all().models().toList())
        assertEquals(listOf("Salomon BRYS: Saint Julien En Genevois", "Laila BRYS-ATIE: Pointe À Pitre"), db.find<Birth>().all().models() .map { db[it.adult]!!.fullName + ": " + db[it.city]!!.name } .toList())
        assertEquals(listOf(Models.sjeg, Models.paris, Models.pap), db.find<City>().all().models().toList())
    }

    @Test
    fun test02_findById() {
        db.inflateDB()

        assertEquals(listOf(Models.salomon), db.find<Adult>().byId("BRYS").models().toList())
    }

    @Test
    fun test03_findByIdOpen() {
        db.inflateDB()

        assertEquals(listOf(Models.salomon, Models.laila), db.find<Adult>().byId("BRYS", isOpen = true).models().toList())
    }

    @Test
    fun test04_findAllByIndex() {
        db.inflateDB()

        assertEquals(listOf(Models.paris, Models.pap, Models.sjeg), db.find<City>().byIndex("name").models().toList())
    }

    @Test
    fun test05_findByIndex() {
        db.inflateDB()

        assertEquals(listOf(Models.paris), db.find<City>().byIndex("name", "Paris").models().toList())
    }

    @Test
    fun test06_findByIndexOpen() {
        db.inflateDB()

        assertEquals(listOf(Models.paris, Models.pap), db.find<City>().byIndex("name", "P", isOpen = true).models().toList())
    }

    @Test
    fun test07_getIndexes() {
        db.inflateDB()

        assertEquals(listOf("firstName", "birth"), db.getIndexesOf(db.newKeyFrom(Models.salomon)))
    }

    @Test
    fun test08_entries() {
        db.inflateDB()

        val all = db.findAll().entries().toList()
        assertEquals<Map<Key<Any>, Any>>(hashMapOf(db.newKeyFrom(Models.salomon) to Models.salomon, db.newKeyFrom(Models.laila) to Models.laila), all.subList(0, 2).associate { it.key to it.model })
        assertEquals<Map<Key<Any>, Any>>(hashMapOf(db.newKeyFrom(Models.sjeg) to Models.sjeg, db.newKeyFrom(Models.paris) to Models.paris, db.newKeyFrom(Models.pap) to Models.pap), all.subList(4, 7).associate { it.key to it.model })
    }

}
