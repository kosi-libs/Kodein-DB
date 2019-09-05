package org.kodein.db.data

import org.kodein.db.BaseCursor
import org.kodein.db.TransientBytes
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadBuffer

interface DataCursor : BaseCursor {

    fun nextEntries(size: Int): Entries

    fun transientKey(): TransientBytes
    fun transientValue(): TransientBytes

    interface Entries : BaseCursor.BaseEntries {
        fun key(i: Int): ReadBuffer
        operator fun get(i: Int): ReadBuffer
    }

}
