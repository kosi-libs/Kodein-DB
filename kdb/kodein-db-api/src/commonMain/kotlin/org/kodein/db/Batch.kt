package org.kodein.db

import org.kodein.memory.Closeable


public interface Batch : DBWrite, Closeable {
    public fun addOptions(vararg options: Options.Write)
    public fun Options.Write.unaryPlus(): Unit = addOptions(this)
    public fun write(vararg options: Options.Write)
}
