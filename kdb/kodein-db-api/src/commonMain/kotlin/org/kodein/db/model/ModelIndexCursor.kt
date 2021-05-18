package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.memory.io.ReadMemory
import kotlin.reflect.KClass


public interface ModelIndexCursor<M : Any> : ModelCursor<M> {

    public fun transientAssociatedData(): ReadMemory?

}
