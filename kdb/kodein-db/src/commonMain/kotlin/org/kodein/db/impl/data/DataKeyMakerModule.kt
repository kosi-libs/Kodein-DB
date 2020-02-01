package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataKeyMaker
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.array

internal interface DataKeyMakerModule : DataKeyMaker {

    override fun newKey(type: ReadMemory, id: Value): KBuffer =
            KBuffer.array(getObjectKeySize(type.limit, id)) { putObjectKey(type, id) }

}
