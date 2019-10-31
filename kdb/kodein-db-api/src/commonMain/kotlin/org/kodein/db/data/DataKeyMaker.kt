package org.kodein.db.data

import org.kodein.db.Value
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.KBuffer

interface DataKeyMaker {

    fun newHeapKey(type: String, id: Value): KBuffer

    fun newNativeKey(type: String, id: Value): Allocation

}
