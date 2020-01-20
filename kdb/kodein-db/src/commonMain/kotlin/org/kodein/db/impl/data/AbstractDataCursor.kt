package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.impl.utils.compareTo
import org.kodein.db.impl.utils.startsWith
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.*

internal abstract class AbstractDataCursor(protected val it: LevelDB.Cursor, private val prefix: ByteArray) : DataCursor {

    private var cachedValid: Boolean? = null
    private var cachedItKey: KBuffer? = null
    private var cachedValue: Allocation? = null

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

    protected fun itKey(): KBuffer {
        return cachedItKey ?: it.transientKey().also { cachedItKey = it }
    }

    override fun close() {
        cachedValid = false
        lastKey?.close()

        it.close()
        cacheReset()
    }

    final override fun isValid(): Boolean {
        return cachedValid ?: (it.isValid() && itKey().startsWith(prefix)).also { cachedValid = it }
    }

    final override fun next() {
        cacheReset()
        it.next()
    }

    final override fun prev() {
        cacheReset()
        it.prev()
    }

    final override fun seekTo(target: ReadBuffer) {
        if (!target.hasRemaining())
            return
        cacheReset()
        if (!target.startsWith(prefix)) {
            if (target > prefix) {
                seekToLast()
                if (isValid())
                    next()
            } else
                seekToFirst()
        } else {
            it.seekTo(target)
        }
    }

    final override fun seekToFirst() {
        cacheReset()
        it.seekTo(KBuffer.wrap(prefix))
    }

    final override fun seekToLast() {
        cacheReset()
        if (lastKey == null) {
            lastKey = Allocation.native(prefix.size + CORK.size) {
                putBytes(prefix)
                putBytes(CORK)
            }
        }
        it.seekTo(lastKey!!)
        while (it.isValid() && it.transientKey().startsWith(prefix))
            it.next()
        if (it.isValid())
            it.prev()
        else
            it.seekToLast()
    }

    protected abstract fun thisKey(): KBuffer

    final override fun transientKey(): ReadBuffer {
        check(isValid()) { "Cursor is not valid" }

        return  thisKey().duplicate()
    }

    protected abstract fun thisValue(): Allocation

    final override fun transientValue(): ReadBuffer {
        check(isValid()) { "Cursor is not valid" }

        cachedValue?.let { return it.duplicate() }

        return thisValue().also { cachedValue = it } .duplicate()
    }

    final override fun transientSeekKey(): ReadBuffer {
        check(isValid()) { "Cursor is not valid" }
        return itKey()
    }

    companion object {
        private val CORK = ByteArray(16) { 0xFF.toByte() }
    }

}