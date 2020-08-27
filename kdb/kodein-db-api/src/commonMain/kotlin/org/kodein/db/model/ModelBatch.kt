package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable

public interface ModelBatch : ModelWrite, Closeable {
    public fun write(afterErrors: MaybeThrowable, vararg options: Options.Write)
}
