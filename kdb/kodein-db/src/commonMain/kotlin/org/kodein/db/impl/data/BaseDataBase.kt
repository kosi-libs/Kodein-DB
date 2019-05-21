package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataBase
import org.kodein.memory.KBuffer
import org.kodein.memory.array

interface BaseDataBase : DataBase {

    override fun getKey(type: String, primaryKey: Value): KBuffer =
            KBuffer.array(getObjectKeySize(type, primaryKey)) { putObjectKey(type, primaryKey) }


}