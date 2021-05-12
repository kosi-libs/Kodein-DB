package org.kodein.db.kv

import org.kodein.db.BaseCursor
import org.kodein.memory.io.ReadMemory


public interface KeyValueCursor : BaseCursor {

    public fun transientKey(): ReadMemory
    public fun transientValue(): ReadMemory

}
