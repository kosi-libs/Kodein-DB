package org.kodein.db.data

import org.kodein.db.Options
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable


public interface DataBatch : DataWrite, Closeable {
    public fun write(afterErrors: MaybeThrowable, vararg options: Options.Write)
}
