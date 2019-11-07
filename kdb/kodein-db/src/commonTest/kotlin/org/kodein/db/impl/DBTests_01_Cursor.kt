package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.impl.model.Adult
import org.kodein.db.impl.model.Birth
import org.kodein.db.impl.model.City
import org.kodein.db.model.DBSerializer
import org.kodein.db.model.DBTypeTable
import org.kodein.db.model.cache.ModelCache
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Suppress("ClassName")
class DBTests_01_Cursor : DBTests() {

    val typeTable = TypeTable {
        root<Adult>()
        root<Birth>()
        root<City>()
    }

    override fun options(): Array<out Options.Open> = arrayOf(DBSerializer(kxSerializer), DBTypeTable(typeTable), ModelCache.Disable)

    @Test
    fun test00_entries() {
        inflateDB()

        val all = db.findAll().entries().toList()
        assertEquals<Map<Key<Any>, Any>>(hashMapOf(db.newKey(Models.salomon) to Models.salomon, db.newKey(Models.laila) to Models.laila), all.subList(0, 2).associate { it.key to it.model })
        assertEquals<Map<Key<Any>, Any>>(hashMapOf(db.newKey(Models.sjeg) to Models.sjeg, db.newKey(Models.paris) to Models.paris, db.newKey(Models.pap) to Models.pap), all.subList(4, 7).associate { it.key to it.model })
    }

}
