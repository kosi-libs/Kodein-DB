package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.memory.Closeable
import org.kodein.memory.cache.Sized
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

    fun transientSeekKey(): TransientSeekKey

    interface Entries<M: Any> : Closeable {
        val size: Int
        fun getSeekKey(i: Int): ReadBuffer
        fun getKey(i: Int): Key<M>
        fun getModel(i: Int, vararg options: Options.Read): Sized<M>
    }

}
