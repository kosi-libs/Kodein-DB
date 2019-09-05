package org.kodein.db

import org.kodein.memory.Closeable

interface BaseBatch : Closeable {
    fun write(vararg options: Options.Write)
}
