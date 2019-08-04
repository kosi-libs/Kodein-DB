package org.kodein.db.model

import org.kodein.db.*
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadBuffer

interface ModelCursor<M : Any> : Closeable {

    fun isValid(): Boolean

    fun next()
    fun prev()

    fun nextEntries(size: Int): Entries<M>

    fun seekToFirst()
    fun seekToLast()
    fun seekTo(target: ReadBuffer)

    fun transientKey(): TransientKey<M>
    fun model(vararg options: Options.Read): Sized<M>

    fun transientSeekKey(): TransientBytes

    interface Entries<M: Any> : Closeable {
        val size: Int
        fun seekKey(i: Int): ReadBuffer
        fun key(i: Int): Key<M>
        operator fun get(i: Int, vararg options: Options.Read): Sized<M>
    }

}
