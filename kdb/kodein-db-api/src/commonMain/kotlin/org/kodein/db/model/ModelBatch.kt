package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable

interface ModelBatch : ModelWrite, Closeable {
    fun write(afterErrors: MaybeThrowable, vararg options: Options.Write)
}
