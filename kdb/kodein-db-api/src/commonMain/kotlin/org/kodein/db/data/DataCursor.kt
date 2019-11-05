package org.kodein.db.data

import org.kodein.db.BaseCursor
import org.kodein.memory.io.ReadBuffer

interface DataCursor : BaseCursor {

    fun nextEntries(size: Int): Entries

    fun transientKey(): ReadBuffer
    fun transientValue(): ReadBuffer

    interface Entries : BaseCursor.BaseEntries {
        fun key(i: Int): ReadBuffer
        operator fun get(i: Int): ReadBuffer
    }

}
