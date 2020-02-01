package org.kodein.db.data

import org.kodein.db.BaseCursor
import org.kodein.memory.io.ReadMemory

interface DataCursor : BaseCursor {

    fun transientKey(): ReadMemory
    fun transientValue(): ReadMemory

}
