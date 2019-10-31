package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataKeyMaker
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.array
import org.kodein.memory.io.native

internal interface DataKeyMakerModule : DataKeyMaker {

    override fun newHeapKey(type: String, id: Value): KBuffer =
            KBuffer.array(getObjectKeySize(type, id)) { putObjectKey(type, id) }

    override fun newNativeKey(type: String, id: Value): Allocation =
            Allocation.native(getObjectKeySize(type, id)) { putObjectKey(type, id) }

}
