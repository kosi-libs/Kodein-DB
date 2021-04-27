package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataIndexCursor
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.native

internal class DataIndexCursorImpl internal constructor(private val ldb: LevelDB, cursor: LevelDB.Cursor, prefix: ByteArray, options: LevelDB.ReadOptions) : AbstractDataCursor(cursor, prefix), DataIndexCursor {

    private var cachedDocKey: Allocation? = null
    private var cachedMetadata: Array<ReadMemory?>? = null

    private val options: LevelDB.ReadOptions = if (options.snapshot == null) options.copy(snapshot = ldb.newSnapshot()) else options

    override fun cacheReset() {
        super.cacheReset()

        cachedDocKey?.close()
        cachedDocKey = null

        cachedMetadata = null
    }

    override fun close() {
        super.close()

        options.snapshot?.close()
    }

    override fun thisKey(): ReadMemory {
        cachedDocKey?.let { return it }

        val id = Value.of(getIndexDocumentId(cursor.transientKey(), cursor.transientValue()))
        val type = getIndexKeyDocumentType(cursor.transientKey())
        return Allocation.native(getDocumentKeySize(id)) { writeDocumentKey(type, id) }
            .also { cachedDocKey = it }
    }

    override fun thisValue() = ldb.get(thisKey(), options) ?: error("Inconsistent internal state: Index entry points to invalid document entry")

    override fun duplicate(): DataCursor = DataIndexCursorImpl(ldb, ldb.newCursor(options), prefix, options).also {
        it.cursor.seekTo(it.cursor.transientKey())
    }

    override fun transientMetadata(): ReadMemory? {
        check(isValid()) { "Cursor is not valid" }

        cachedMetadata?.let { return it[0] }

        return getIndexBodyMetadata(cursor.transientValue())?.also { cachedMetadata = arrayOf(it) }
    }

}
