package org.kodein.db.impl.data

import org.kodein.db.kv.KeyValueCursor
import org.kodein.memory.io.asManagedAllocation

internal class DataCursorImpl internal constructor(cursor: KeyValueCursor, prefix: ByteArray) : AbstractDataCursor(cursor, prefix) {

    override fun thisKey() = itKey()

    override fun thisValue() = cursor.transientValue().asManagedAllocation()

}
