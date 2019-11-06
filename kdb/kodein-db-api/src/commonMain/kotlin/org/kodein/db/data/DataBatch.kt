package org.kodein.db.data

import org.kodein.db.Options
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable


interface DataBatch : DataWrite, Closeable {
    fun write(afterErrors: MaybeThrowable, vararg options: Options.Write)
}
