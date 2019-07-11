package org.kodein.db.data

import org.kodein.memory.Closeable
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadBuffer

interface DataCursor : Closeable {

    fun isValid(): Boolean

    fun next()
    fun prev()

    fun nextEntries(size: Int): Entries

    fun seekToFirst()
    fun seekToLast()
    fun seekTo(target: ReadBuffer)

    fun transientKey(): KBuffer
    fun transientValue(): KBuffer

    fun transientSeekKey(): KBuffer

    interface Entries : Closeable {
        val size: Int
        fun getSeekKey(i: Int): KBuffer
        fun getKey(i: Int): KBuffer
        fun getValue(i: Int): KBuffer
    }

}
