package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.asManagedAllocation

internal class DataSimpleCursor internal constructor(it: LevelDB.Cursor, prefix: Allocation) : AbstractDataCursor(it, prefix) {

    override fun nextEntries(size: Int): DataCursor.Entries {
        cacheReset()
        return Entries(it.nextArray(size))
    }

    override fun thisKey() = itKey()

    override fun thisValue() = it.transientValue().asManagedAllocation()

    private inner class Entries internal constructor(array: LevelDB.Cursor.ValuesArray) : AbstractEntries<LevelDB.Cursor.ValuesArray>(array) {

        override fun thisSeekKey(i: Int) = arrayKey(i)

        override fun thisKey(i: Int) = arrayKey(i)

        override fun thisValue(i: Int) = array.getValue(i)
    }
}
