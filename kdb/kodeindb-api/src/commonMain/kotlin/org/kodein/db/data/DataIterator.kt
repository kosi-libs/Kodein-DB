package org.kodein.db.data

import kotlinx.io.core.Closeable
import org.kodein.db.leveldb.Bytes

interface DataIterator : Closeable {

    fun isValid(): Boolean

    fun next()
    fun prev()

    fun nextEntries(size: Int): Entries

    fun seekToFirst()
    fun seekToLast()
    fun seekTo(target: Bytes)

    fun version(): Int
    fun transientKey(): Bytes
    fun transientValue(): Bytes

    fun transientSeekKey(): Bytes

    interface Entries : Closeable {
        val size: Int
        fun getSeekKey(i: Int): Bytes
        fun getVersion(i: Int): Int
        fun getKey(i: Int): Bytes
        fun getValue(i: Int): Bytes
    }

}
