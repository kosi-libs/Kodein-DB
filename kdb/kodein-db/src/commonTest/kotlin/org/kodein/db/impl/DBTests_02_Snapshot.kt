package org.kodein.db.impl

import org.kodein.db.delete
import org.kodein.db.get
import org.kodein.db.useSnaphost
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@Suppress("ClassName")
open class DBTests_02_Snapshot : DBTests() {

    @Test
    fun test00_snapshot() {
        db.inflateDB()

        val me = db.newKeyFrom(Models.salomon)

        db.useSnaphost { snapshot ->
            db.delete(me)
            assertNull(db[me])
            assertNotNull(snapshot[me])
        }
    }


}
