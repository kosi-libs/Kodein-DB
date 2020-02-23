package org.kodein.db.data

import org.kodein.db.Value
import org.kodein.memory.io.KBuffer

interface DataKeyMaker {

    fun newKey(type: Int, id: Value): KBuffer

}
