package org.kodein.db.data

import org.kodein.memory.io.ReadMemory


public interface DataIndexCursor : DataCursor {

    public fun transientAssociatedData(): ReadMemory?

}
