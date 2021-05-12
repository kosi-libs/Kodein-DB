package org.kodein.db.impl.data

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.data.DataIndexCursor
import org.kodein.db.kv.KeyValueCursor
import org.kodein.db.kv.KeyValueSnapshot
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.native

internal class DataIndexCursorImpl internal constructor(
    private val snapshot: KeyValueSnapshot,
    private val closeSnapshot: Boolean,
    cursor: KeyValueCursor,
    prefix: ByteArray,
    private val getOptions: Array<out Options.Get>
) : AbstractDataCursor(cursor, prefix), DataIndexCursor {

    private var cachedDocKey: Allocation? = null
    private var cachedAssociatedData: Array<ReadMemory?>? = null

    override fun cacheReset() {
        super.cacheReset()

        cachedDocKey?.close()
        cachedDocKey = null

        cachedAssociatedData = null
    }

    override fun close() {
        super.close()

        if (closeSnapshot) snapshot.close()
    }

    override fun thisKey(): ReadMemory {
        cachedDocKey?.let { return it }

        val id = Value.of(getIndexDocumentId(cursor.transientKey(), cursor.transientValue()))
        val type = getIndexKeyDocumentType(cursor.transientKey())
        return Allocation.native(getDocumentKeySize(id)) { writeDocumentKey(type, id) }
            .also { cachedDocKey = it }
    }

    override fun thisValue() = snapshot.get(thisKey(), *getOptions) ?: error("Inconsistent internal state: Index entry points to invalid document entry")

    override fun transientAssociatedData(): ReadMemory? {
        check(isValid()) { "Cursor is not valid" }

        cachedAssociatedData?.let { return it[0] }

        return getIndexBodyAssociatedData(cursor.transientValue())?.also { cachedAssociatedData = arrayOf(it) }
    }

}
