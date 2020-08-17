package org.kodein.db.data

import org.kodein.db.BaseCursor
import org.kodein.memory.io.ReadMemory

public interface DataCursor : BaseCursor {

    public fun transientKey(): ReadMemory
    public fun transientValue(): ReadMemory

    public fun duplicate(): DataCursor

}
