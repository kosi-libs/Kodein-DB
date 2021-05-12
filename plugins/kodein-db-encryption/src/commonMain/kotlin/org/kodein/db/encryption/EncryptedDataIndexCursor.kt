package org.kodein.db.encryption

import org.kodein.db.Options
import org.kodein.db.data.DataIndexCursor
import org.kodein.memory.io.ReadAllocation
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.asManagedAllocation


internal class EncryptedDataIndexCursor(
    eddb: EncryptedDataDB, override val cursor: DataIndexCursor, options: Array<out Options.Find>
) : EncryptedDataCursor(eddb, cursor, options), DataIndexCursor {

    private var currentMetadata: ReadAllocation? = null

    override fun reset() {
        currentMetadata?.close()
        currentMetadata = null
        super.reset()
    }

    override fun transientAssociatedData(): ReadMemory? {
        currentMetadata?.let {  return it }

        val md = cursor.transientAssociatedData() ?: return null

        return eddb.decrypt(transientKey(), md.asManagedAllocation(), docKeyOption)
            .also { currentMetadata = it }
    }

}
