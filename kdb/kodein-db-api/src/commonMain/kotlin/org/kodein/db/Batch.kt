package org.kodein.db

import org.kodein.memory.Closeable


interface Batch : DBWrite, Closeable {
    fun addOptions(vararg options: Options.Write)
    fun Options.Write.unaryPlus() = addOptions(this)
    fun write(vararg options: Options.Write)
}
