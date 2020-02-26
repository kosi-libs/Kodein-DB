package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.ReadBuffer

internal class DataIndexCursor internal constructor(private val ldb: LevelDB, cursor: LevelDB.Cursor, prefix: ByteArray, options: LevelDB.ReadOptions) : AbstractDataCursor(cursor, prefix) {

    private var cachedItValue: ReadBuffer? = null

    private val options: LevelDB.ReadOptions = if (options.snapshot == null) options.copy(snapshot = ldb.newSnapshot()) else options

    override fun cacheReset() {
        super.cacheReset()

        cachedItValue = null
    }

    private fun itValue() = cachedItValue ?: cursor.transientValue().also { cachedItValue = it }

    override fun close() {
        super.close()

        options.snapshot?.close()
    }

    override fun thisKey() = itValue()

    override fun thisValue() = ldb.get(itValue(), options) ?: throw IllegalStateException("Index entry points to invalid object entry")

    override fun duplicate(): DataCursor = DataIndexCursor(ldb, ldb.newCursor(options), prefix, options).also {
        it.cursor.seekTo(it.cursor.transientKey())
    }
}
