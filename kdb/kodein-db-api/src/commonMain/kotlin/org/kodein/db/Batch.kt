package org.kodein.db

import org.kodein.memory.Closeable


interface Batch : DBWrite, Closeable {
    fun write(vararg options: Options.Write)
}
