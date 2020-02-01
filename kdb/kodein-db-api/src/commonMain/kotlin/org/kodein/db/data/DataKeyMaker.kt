package org.kodein.db.data

import org.kodein.db.Value
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory

interface DataKeyMaker {

    fun newKey(type: ReadMemory, id: Value): KBuffer

}
