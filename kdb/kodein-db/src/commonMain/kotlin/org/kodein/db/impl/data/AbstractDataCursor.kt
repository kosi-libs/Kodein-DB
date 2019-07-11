package org.kodein.db.impl.data

import org.kodein.db.data.DataCursor
import org.kodein.db.impl.utils.compareTo
import org.kodein.db.impl.utils.startsWith
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.*

internal abstract class AbstractDataCursor(protected val it: LevelDB.Cursor, private val prefix: Allocation) : DataCursor {

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
        prefix.close()
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
        it.seekTo(prefix)
    }

    final override fun seekToLast() {
        cacheReset()
        if (lastKey == null) {
            val key = Allocation.native(prefix.remaining + CORK.size)

            key.putBytes(prefix.duplicate())
            key.putBytes(CORK)
            key.flip()

            lastKey = key
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

    final override fun transientKey(): KBuffer {
        if (!isValid())
            throw IllegalStateException("Cursor is not valid")

        return thisKey().duplicate()
    }

    protected abstract fun thisValue(): Allocation

    final override fun transientValue(): KBuffer {
        if (!isValid())
            throw IllegalStateException("Cursor is not valid")

        cachedValue?.let { return it.duplicate() }

        return thisValue().also { cachedValue = it } .duplicate()
    }

    final override fun transientSeekKey(): KBuffer {
        if (!isValid())
            throw IllegalStateException("Cursor is not valid")
        return itKey()
    }

    abstract class AbstractEntries<A: LevelDB.Cursor.ValuesArrayBase>(protected val array: A) : DataCursor.Entries {
        private val cachedArrayKeys = arrayOfNulls<KBuffer>(array.size)
        private val cachedValues = arrayOfNulls<KBuffer>(array.size)

        final override val size get() = array.size

        protected fun arrayKey(i: Int) = cachedArrayKeys[i] ?: array.getKey(i).also { cachedArrayKeys[i] = it }

        protected abstract fun thisSeekKey(i: Int): KBuffer

        final override fun getSeekKey(i: Int) = thisSeekKey(i).duplicate()

        protected abstract fun thisKey(i: Int): KBuffer

        final override fun getKey(i: Int) = thisKey(i).duplicate()

        protected abstract fun thisValue(i: Int): KBuffer

        final override fun getValue(i: Int): KBuffer {
            cachedValues[i]?.let { return it.duplicate() }

            val value = thisValue(i)
            cachedValues[i] = value
            return value.duplicate()
        }

        final override fun close()  = array.close()

    }

    companion object {
        private val CORK = ByteArray(16) { 0xFF.toByte() }
    }

}