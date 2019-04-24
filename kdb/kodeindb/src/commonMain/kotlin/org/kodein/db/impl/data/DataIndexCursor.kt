package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Allocation
import org.kodein.memory.KBuffer
import org.kodein.memory.ReadBuffer

class DataIndexCursor internal constructor(private val ddb: DataDBImpl, it: LevelDB.Cursor, prefix: Allocation, options: LevelDB.ReadOptions) : AbstractDataCursor(it, prefix) {

    private var cachedItValue: KBuffer? = null

    private val options: LevelDB.ReadOptions = if (options.snapshot == null) options.copy(snapshot = ddb.ldb.newSnapshot()) else options
    private val boundSnapshot: Boolean = (options.snapshot == null)

    override fun cacheReset() {
        super.cacheReset()

        cachedItValue = null
    }

    private fun itValue() = cachedItValue ?: it.transientValue().also { cachedItValue = it }

    override fun close() {
        super.close()

        if (boundSnapshot)
            options.snapshot!!.close()
    }

    override fun nextEntries(size: Int): DataCursor.Entries {
        cacheReset()
        return Entries(it.nextIndirectArray(ddb.ldb, size))
    }

    override fun thisKey() = itValue()

    override fun thisValue() = ddb.ldb.get(itValue(), options) ?: throw IllegalStateException("Index entry points to invalid object entry")

    private inner class Entries internal constructor(array: LevelDB.Cursor.IndirectValuesArray) : AbstractEntries<LevelDB.Cursor.IndirectValuesArray>(array) {

        private val cachedArrayIntermediateKeys = arrayOfNulls<KBuffer>(array.size)

        override fun thisSeekKey(i: Int) = arrayKey(i)

        override fun thisKey(i: Int) = cachedArrayIntermediateKeys[i] ?: array.getIntermediateKey(i).also { cachedArrayIntermediateKeys[i] = it }

        override fun thisValue(i: Int) = array.getValue(i) ?: throw IllegalStateException("Index entry points to invalid object entry")
    }
}
