package org.kodein.db.data

import org.kodein.db.Value
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.KBuffer

interface DataBase {

    fun getHeapKey(type: String, primaryKey: Value): KBuffer

    fun getNativeKey(type: String, primaryKey: Value): Allocation

}
