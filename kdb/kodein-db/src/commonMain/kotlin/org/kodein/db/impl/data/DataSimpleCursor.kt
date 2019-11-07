package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.asManagedAllocation

internal class DataSimpleCursor internal constructor(it: LevelDB.Cursor, prefix: Allocation) : AbstractDataCursor(it, prefix) {

    override fun thisKey() = itKey()

    override fun thisValue() = it.transientValue().asManagedAllocation()

}
