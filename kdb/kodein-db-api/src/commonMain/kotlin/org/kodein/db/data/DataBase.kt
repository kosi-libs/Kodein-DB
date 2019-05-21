package org.kodein.db.data

import org.kodein.db.Value
import org.kodein.memory.KBuffer

interface DataBase {

    fun getKey(type: String, primaryKey: Value): KBuffer

}
