package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.kv.KeyValueCursor
import org.kodein.memory.io.*

internal abstract class AbstractDataCursor(protected val cursor: KeyValueCursor, protected val prefix: ByteArray) : DataCursor {

    private var cachedValid: Boolean? = null
    private var cachedItKey: ReadMemory? = null
    private var cachedValue: ReadAllocation? = null

    private var lastKey: Allocation? = null

    init {
        seekToFirst()
    }

    protected open fun cacheReset() {
        cachedValid = null
        cachedItKey = null
        cachedValue?.close()
        cachedValue = null
    }

    protected fun itKey(): ReadMemory {
        return cachedItKey ?: cursor.transientKey().also { cachedItKey = it }
    }

    override fun close() {
        cachedValid = false
        lastKey?.close()

        cursor.close()
        cacheReset()
    }

    final override fun isValid(): Boolean =
        cachedValid ?: (cursor.isValid() && isValidSeekKey(itKey())).also { cachedValid = it }

    final override fun next() {
        cacheReset()
        cursor.next()
    }

    final override fun prev() {
        cacheReset()
        cursor.prev()
    }

    private fun isValidSeekKey(key: ReadMemory) = key.startsWith(prefix)

    final override fun seekTo(target: ReadMemory) {
        cacheReset()
        check(isValidSeekKey(target)) { "Not a valid seek key" }
        cursor.seekTo(target)
    }

    final override fun seekToFirst() {
        cacheReset()
        cursor.seekTo(Memory.wrap(prefix))
    }

    final override fun seekToLast() {
        cacheReset()
        if (lastKey == null) {
            val prefix = prefix
            lastKey = Allocation.native(prefix.size + CORK.size) {
                writeBytes(prefix)
                writeBytes(CORK)
            }
        }
        cursor.seekTo(lastKey!!)
        while (cursor.isValid() && isValidSeekKey(cursor.transientKey()))
            cursor.next()
        if (cursor.isValid())
            cursor.prev()
        else
            cursor.seekToLast()
    }

    protected abstract fun thisKey(): ReadMemory

    final override fun transientKey(): ReadMemory {
        check(isValid()) { "Cursor is not valid" }
        return thisKey()
    }

    protected abstract fun thisValue(): ReadAllocation

    final override fun transientValue(): ReadMemory {
        check(isValid()) { "Cursor is not valid" }

        cachedValue?.let { return it }

        return thisValue().also { cachedValue = it }
    }

    final override fun transientSeekKey(): ReadMemory {
        check(isValid()) { "Cursor is not valid" }
        return itKey()
    }

    companion object {
        private val CORK = ByteArray(16) { 0xFF.toByte() }
    }

}