package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataKeyMaker
import org.kodein.memory.io.Memory
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.array

internal interface DataKeyMakerModule : DataKeyMaker {

    override fun newKey(type: Int, id: Value): ReadMemory =
        Memory.array(getDocumentKeySize(id)) { writeDocumentKey(type, id) }

}
