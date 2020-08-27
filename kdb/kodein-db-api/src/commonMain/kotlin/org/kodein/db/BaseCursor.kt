package org.kodein.db

import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory

public interface BaseCursor : Closeable {

    public fun isValid(): Boolean

    public fun next()
    public fun prev()

    public fun seekToFirst()
    public fun seekToLast()
    public fun seekTo(target: ReadMemory)

    public fun transientSeekKey(): ReadMemory

}
