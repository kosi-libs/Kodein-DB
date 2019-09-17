package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataKeyMaker
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.array
import org.kodein.memory.io.native

internal interface DataKeyMakerModule : DataKeyMaker {

    override fun getHeapKey(type: String, primaryKey: Value): KBuffer =
            KBuffer.array(getObjectKeySize(type, primaryKey)) { putObjectKey(type, primaryKey) }

    override fun getNativeKey(type: String, primaryKey: Value): Allocation =
            Allocation.native(getObjectKeySize(type, primaryKey)) { putObjectKey(type, primaryKey) }

}
