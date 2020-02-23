package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.*

internal abstract class AbstractDataCursor(protected val it: LevelDB.Cursor, protected val prefix: ByteArray) : DataCursor {

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
        return cachedItKey ?: it.transientKey().also { cachedItKey = it }
    }

    override fun close() {
        cachedValid = false
        lastKey?.close()

        it.close()
        cacheReset()
    }

    final override fun isValid(): Boolean {
        return cachedValid ?: (it.isValid() && isValidSeekKey(itKey())).also { cachedValid = it }
    }

    final override fun next() {
        cacheReset()
        it.next()
    }

    final override fun prev() {
        cacheReset()
        it.prev()
    }

    protected abstract fun isValidSeekKey(key: ReadMemory): Boolean

    final override fun seekTo(target: ReadMemory) {
        cacheReset()
        require(isValidSeekKey(target)) { "Not a valid seek key" }
        it.seekTo(target)
    }

    final override fun seekToFirst() {
        cacheReset()
        it.seekTo(KBuffer.wrap(prefix))
    }

    final override fun seekToLast() {
        cacheReset()
        if (lastKey == null) {
            val prefix = prefix
            lastKey = Allocation.native(prefix.size + CORK.size) {
                putBytes(prefix)
                putBytes(CORK)
            }
        }
        it.seekTo(lastKey!!)
        while (it.isValid() && isValidSeekKey(it.transientKey()))
            it.next()
        if (it.isValid())
            it.prev()
        else
            it.seekToLast()
    }

    protected abstract fun thisKey(): ReadMemory

    final override fun transientKey(): ReadMemory {
        check(isValid()) { "Cursor is not valid" }

        return  thisKey().duplicate()
    }

    protected abstract fun thisValue(): ReadAllocation

    final override fun transientValue(): ReadMemory {
        check(isValid()) { "Cursor is not valid" }

        cachedValue?.let { return it.duplicate() }

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