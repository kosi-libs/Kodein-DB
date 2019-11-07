package org.kodein.db.data

import org.kodein.db.BaseCursor
import org.kodein.memory.io.ReadBuffer

interface DataCursor : BaseCursor {

    fun transientKey(): ReadBuffer
    fun transientValue(): ReadBuffer

}
