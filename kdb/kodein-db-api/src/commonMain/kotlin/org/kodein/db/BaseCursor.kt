package org.kodein.db

import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory

interface BaseCursor : Closeable {

    fun isValid(): Boolean

    fun next()
    fun prev()

    fun seekToFirst()
    fun seekToLast()
    fun seekTo(target: ReadMemory)

    fun transientSeekKey(): ReadMemory

}
