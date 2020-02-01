package org.kodein.db.impl.data

import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.asManagedReadAllocation

internal class DataSimpleCursor internal constructor(it: LevelDB.Cursor, prefix: ByteArray) : AbstractDataCursor(it, prefix) {

    override fun thisKey() = itKey()

    override fun thisValue() = it.transientValue().asManagedReadAllocation()

}
