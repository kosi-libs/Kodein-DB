package org.kodein.db.data

import org.kodein.db.TransientBytes
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadBuffer

interface DataCursor : Closeable {

    fun isValid(): Boolean

    fun next()
    fun prev()

    fun nextEntries(size: Int): Entries

    fun seekToFirst()
    fun seekToLast()
    fun seekTo(target: ReadBuffer)

    fun transientKey(): TransientBytes
    fun transientValue(): TransientBytes

    fun transientSeekKey(): TransientBytes

    interface Entries : Closeable {
        val size: Int
        fun seekKey(i: Int): ReadBuffer
        fun key(i: Int): ReadBuffer
        operator fun get(i: Int): ReadBuffer
    }

}
