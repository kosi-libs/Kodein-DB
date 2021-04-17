package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.asManagedAllocation

internal class DataSimpleCursor internal constructor(val ldb: LevelDB, cursor: LevelDB.Cursor, prefix: ByteArray) : AbstractDataCursor(cursor, prefix) {

    override fun thisKey() = itKey()

    override fun thisValue() = cursor.transientValue().asManagedAllocation()

    override fun duplicate(): DataCursor = DataSimpleCursor(ldb, ldb.newCursor(), prefix).also {
        it.cursor.seekTo(it.cursor.transientKey())
    }
}
