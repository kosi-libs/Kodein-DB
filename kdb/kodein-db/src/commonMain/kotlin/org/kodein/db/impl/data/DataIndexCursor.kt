package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.KBuffer

internal class DataIndexCursor internal constructor(private val ldb: LevelDB, it: LevelDB.Cursor, prefix: Allocation, options: LevelDB.ReadOptions) : AbstractDataCursor(it, prefix) {

    private var cachedItValue: KBuffer? = null

    private val options: LevelDB.ReadOptions = if (options.snapshot == null) options.copy(snapshot = ldb.newSnapshot()) else options

    override fun cacheReset() {
        super.cacheReset()

        cachedItValue = null
    }

    private fun itValue() = cachedItValue ?: it.transientValue().also { cachedItValue = it }

    override fun close() {
        super.close()

        options.snapshot?.close()
    }

    override fun nextEntries(size: Int): DataCursor.Entries {
        cacheReset()
        return Entries(it.nextIndirectArray(ldb, size))
    }

    override fun thisKey() = itValue()

    override fun thisValue() = ldb.get(itValue(), options) ?: throw IllegalStateException("Index entry points to invalid object entry")

    private inner class Entries internal constructor(array: LevelDB.Cursor.IndirectValuesArray) : AbstractEntries<LevelDB.Cursor.IndirectValuesArray>(array) {

        private val cachedArrayIntermediateKeys = arrayOfNulls<KBuffer>(array.size)

        override fun thisSeekKey(i: Int) = arrayKey(i)

        override fun thisKey(i: Int) = cachedArrayIntermediateKeys[i] ?: array.getIntermediateKey(i).also { cachedArrayIntermediateKeys[i] = it }

        override fun thisValue(i: Int) = array.getValue(i) ?: throw IllegalStateException("Index entry points to invalid object entry")
    }
}
