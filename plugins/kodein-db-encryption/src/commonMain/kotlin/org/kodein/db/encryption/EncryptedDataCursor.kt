package org.kodein.db.encryption

import org.kodein.db.Options
import org.kodein.db.data.DataCursor
import org.kodein.db.invoke
import org.kodein.memory.io.*


internal open class EncryptedDataCursor(internal val eddb: EncryptedDataDB, internal open val cursor: DataCursor, options: Array<out Options.Find>): DataCursor by cursor {

    internal val docKeyOption: EncryptedDocumentKey? = options()

    private var currentValue: ReadAllocation? = null

    internal open fun reset() {
        currentValue?.close()
        currentValue = null
    }

    override fun next() {
        reset()
        cursor.next()
    }

    override fun prev() {
        reset()
        cursor.prev()
    }

    override fun seekToFirst() {
        reset()
        cursor.seekToFirst()
    }

    override fun seekToLast() {
        reset()
        cursor.seekToLast()
    }

    override fun seekTo(target: ReadMemory) {
        reset()
        cursor.seekTo(target)
    }

    override fun transientValue(): ReadMemory {
        currentValue?.let {  return it }

        return eddb.decrypt(transientKey(), cursor.transientValue().asManagedAllocation(), docKeyOption)
            .also { currentValue = it }
    }

    override fun close() {
        reset()
        cursor.close()
    }
}