package org.kodein.db.impl.data

import kotlinx.io.core.writeFully
import org.kodein.db.data.DataIterator
import org.kodein.db.impl.utils.compareTo
import org.kodein.db.impl.utils.startsWith
import org.kodein.db.leveldb.Allocation
import org.kodein.db.leveldb.Bytes
import org.kodein.db.leveldb.LevelDB

abstract class AbstractDataIterator(protected val it: LevelDB.Iterator, protected val prefix: Bytes) : DataIterator {

    private var cachedValid: Boolean? = null
    private var cachedItKey: Bytes? = null
    private var cachedValue: Allocation? = null
    private var cachedVersion = -1

    private var lastKey: Allocation? = null

    init {
        seekToFirst()
    }

    protected open fun cacheReset() {
        cachedValid = null
        cachedItKey = null
        cachedVersion = -1
        cachedValue?.close()
        cachedValue = null
    }

    protected fun itKey(): Bytes {
        return cachedItKey ?: it.transientKey().also { cachedItKey = it }
    }

    override fun close() {
        cachedValid = false
        lastKey?.close()

        it.close()
        cacheReset()
    }

    final override fun isValid(): Boolean {
        return cachedValid ?: (it.isValid() && itKey().buffer.startsWith(prefix.buffer)).also { cachedValid = it }
    }

    final override fun next() {
        cacheReset()
        it.next()
    }

    final override fun prev() {
        cacheReset()
        it.prev()
    }

    final override fun seekTo(target: Bytes) {
        if (target.buffer.readRemaining == 0)
            return
        cacheReset()
        if (!target.buffer.startsWith(prefix.buffer)) {
            if (target.buffer > prefix.buffer) {
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
            val key = Allocation.allocNativeBuffer(prefix.buffer.readRemaining + CORK.size)

            key.buffer.writeFully(prefix.buffer.makeView())
            key.buffer.writeFully(CORK)

            lastKey = key
        }
        it.seekTo(lastKey!!)
        while (it.isValid() && it.transientKey().buffer.startsWith(prefix.buffer))
            it.next()
        if (it.isValid())
            it.prev()
        else
            it.seekToLast()
    }

    final override fun version(): Int {
        if (cachedVersion < 0)
            transientValue()
        return cachedVersion
    }

    protected abstract fun thisKey(): Bytes

    final override fun transientKey(): Bytes {
        if (!isValid())
            throw IllegalStateException("Iterator is not valid")

        return thisKey().makeView()
    }

    protected abstract fun thisValue(): Allocation

    final override fun transientValue(): Bytes {
        if (!isValid())
            throw IllegalStateException("Iterator is not valid")

        cachedValue?.let { return it.makeView() }

        val value = thisValue()
        cachedVersion = value.buffer.readInt()
        cachedValue = value
        return value.makeView()
    }

    final override fun transientSeekKey(): Bytes {
        if (!isValid())
            throw IllegalStateException("Iterator is not valid")
        return itKey()
    }

    abstract class AbstractEntries<A: LevelDB.Iterator.ValuesArrayBase>(protected val array: A) : DataIterator.Entries {
        private val cachedArrayKeys = arrayOfNulls<Bytes>(array.size)
        private val cachedValues = arrayOfNulls<Bytes>(array.size)
        private val cachedVersions = IntArray(array.size) { -1 }

        final override val size get() = array.size

        final override fun getVersion(i: Int): Int {
            if (cachedVersions[i] < 0)
                getValue(i)
            return cachedVersions[i]
        }

        protected fun arrayKey(i: Int) = cachedArrayKeys[i] ?: array.getKey(i).also { cachedArrayKeys[i] = it }

        protected abstract fun thisSeekKey(i: Int): Bytes

        final override fun getSeekKey(i: Int) = thisSeekKey(i).makeView()

        protected abstract fun thisKey(i: Int): Bytes

        final override fun getKey(i: Int) = thisKey(i).makeView()

        protected abstract fun thisValue(i: Int): Bytes

        final override fun getValue(i: Int): Bytes {
            cachedValues[i]?.let { return it.makeView() }

            val value = thisValue(i)
            cachedVersions[i] = value.buffer.readInt()
            cachedValues[i] = value
            return value.makeView()
        }

        final override fun close()  = array.close()

    }

    companion object {
        private val CORK = byteArrayOf(
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),
                0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()
        )
    }

}