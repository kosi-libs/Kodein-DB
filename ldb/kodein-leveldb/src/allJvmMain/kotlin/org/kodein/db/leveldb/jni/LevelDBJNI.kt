package org.kodein.db.leveldb.jni

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.PlatformCloseable
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.wrap
import java.nio.ByteBuffer
import java.util.*

/**
 * Native implementation of the LevelDB interface, using Google's C++ LevelDB.
 *
 * This is the recommended implementation of [LevelDB] and by far the fastest.
 *
 * However, it requires its native library to be loaded (loading depends on the platform).
 */
@Suppress("FunctionName")
class LevelDBJNI private constructor(ptr: Long, private val optionsPtr: Long, options: LevelDB.Options, override val path: String) : NativeBound(ptr, "DB", null, options), LevelDB {

    private val dbHandler = Handler()

    /**
     * LevelDB Factory that handles native LevelDB databases.
     */
    object Factory : LevelDBFactory {

        override fun open(path: String, options: LevelDB.Options): LevelDB {
            val optionsPtr = newNativeOptions(options)
            try {
                return LevelDBJNI(Native.dbOpen(path, optionsPtr, options.repairOnCorruption), optionsPtr, options, path)
            }
            catch (e: Throwable) {
                Native.optionsRelease(optionsPtr)
                throw e
            }
        }

        override fun destroy(path: String, options: LevelDB.Options) {
            val optionsPtr = newNativeOptions(options)
            try {
                Native.dbDestroy(path, optionsPtr)
            }
            finally {
                Native.optionsRelease(optionsPtr)
            }
        }
    }


    override fun beforeClose() {
        // Before effectively closing the database, we need to close all non-closed related objects.
        dbHandler.close()
    }

    override fun put(key: ReadBuffer, value: ReadBuffer, options: LevelDB.WriteOptions) {
        val directKey = key.directByteBuffer()
        val directValue = value.directByteBuffer()

        if (directKey != null && directValue != null) {
            Native.putBB(nonZeroPtr, directKey, directKey.position(), directKey.remaining(), directValue, directValue.position(), directValue.remaining(), options.sync)
        } else if (directValue != null) {
            val arrayKey = key.array()
            Native.putAB(nonZeroPtr, arrayKey.array, arrayKey.offset, arrayKey.length, directValue, directValue.position(), directValue.remaining(), options.sync)
        } else if (directKey != null) {
            val arrayValue = value.array()
            Native.putBA(nonZeroPtr, directKey, directKey.position(), directKey.remaining(), arrayValue.array, arrayValue.offset, arrayValue.length, options.sync)
        } else {
            val arrayKey = key.array()
            val arrayValue = value.array()
            Native.putAA(nonZeroPtr, arrayKey.array, arrayKey.offset, arrayKey.length, arrayValue.array, arrayValue.offset, arrayValue.length, options.sync)
        }
    }

    override fun delete(key: ReadBuffer, options: LevelDB.WriteOptions) {
        val directKey = key.directByteBuffer()
        if (directKey != null) {
            Native.deleteB(nonZeroPtr, directKey, directKey.position(), directKey.remaining(), options.sync)
        } else {
            val arrayKey = key.array()
            Native.deleteA(nonZeroPtr, arrayKey.array, arrayKey.offset, arrayKey.length, options.sync)
        }
    }

    override fun write(batch: LevelDB.WriteBatch, options: LevelDB.WriteOptions) {
        Native.write(nonZeroPtr, (batch as WriteBatch).nonZeroPtr, options.sync)
    }

    override fun get(key: ReadBuffer, options: LevelDB.ReadOptions): Allocation? {
        val directKey = key.directByteBuffer()
        val valuePtr = if (directKey != null) {
            Native.getB(nonZeroPtr, directKey, directKey.position(), directKey.remaining(), options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        } else {
            val arrayKey = key.array()
            Native.getA(nonZeroPtr, arrayKey.array, arrayKey.offset, arrayKey.length, options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        }
        return if (valuePtr == 0L) null else NativeBytes(valuePtr, dbHandler, this.options)

    }

    override fun indirectGet(key: ReadBuffer, options: LevelDB.ReadOptions): Allocation? {
        val directKey = key.directByteBuffer()
        val valuePtr = if (directKey != null) {
            Native.indirectGetB(nonZeroPtr, directKey, directKey.position(), directKey.remaining(), options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        } else {
            val arrayKey = key.array()
            Native.indirectGetA(nonZeroPtr, arrayKey.array, arrayKey.offset, arrayKey.length, options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        }
        return if (valuePtr == 0L) null else NativeBytes(valuePtr, dbHandler, this.options)
    }

    override fun indirectGet(cursor: LevelDB.Cursor, options: LevelDB.ReadOptions): Allocation? {
        val valuePtr = Native.indirectGetI(nonZeroPtr, (cursor as Cursor).nonZeroPtr, options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        return if (valuePtr == 0L) null else NativeBytes(valuePtr, dbHandler, this.options)
    }

    override fun newCursor(options: LevelDB.ReadOptions): LevelDB.Cursor {
        return Cursor(Native.iteratorNew(nonZeroPtr, options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot)), dbHandler, this.options)
    }

    override fun newSnapshot(): LevelDB.Snapshot {
        val ptr = nonZeroPtr
        return Snapshot(ptr, Native.snapshotNew(ptr), dbHandler, options)
    }

    override fun newWriteBatch(): LevelDB.WriteBatch {
        return WriteBatch(Native.writeBatchNew(), dbHandler, options)
    }

    override fun release(ptr: Long) {
        Native.dbRelease(ptr)
        Native.optionsRelease(optionsPtr)
    }

    private class NativeBytes internal constructor(ptr: Long, handler: Handler, options: LevelDB.Options, val buffer: KBuffer = KBuffer.wrap(Native.bufferNew(ptr)))
        : NativeBound(ptr, "Value", handler, options), Allocation, KBuffer by buffer {

        override fun release(ptr: Long) {
            Native.bufferRelease(ptr)
        }
    }

    private class WriteBatch internal constructor(ptr: Long, handler: Handler, options: LevelDB.Options) : NativeBound(ptr, "WriteBatch", handler, options), LevelDB.WriteBatch {

        override fun put(key: ReadBuffer, value: ReadBuffer) {
            val directKey = key.directByteBuffer()
            val directValue = value.directByteBuffer()

            if (directKey != null && directValue != null) {
                Native.writeBatchPutBB(nonZeroPtr, directKey, directKey.position(), directKey.remaining(), directValue, directValue.position(), directValue.remaining())
            } else if (directValue != null) {
                val arrayKey = key.array()
                Native.writeBatchPutAB(nonZeroPtr, arrayKey.array, arrayKey.offset, arrayKey.length, directValue, directValue.position(), directValue.remaining())
            } else if (directKey != null) {
                val arrayValue = value.array()
                Native.writeBatchPutBA(nonZeroPtr, directKey, directKey.position(), directKey.remaining(), arrayValue.array, arrayValue.offset, arrayValue.length)
            } else {
                val arrayKey = key.array()
                val arrayValue = value.array()
                Native.writeBatchPutAA(nonZeroPtr, arrayKey.array, arrayKey.offset, arrayKey.length, arrayValue.array, arrayValue.offset, arrayValue.length)
            }
        }

        override fun delete(key: ReadBuffer) {
            val directKey = key.directByteBuffer()
            if (directKey != null) {
                Native.writeBatchDeleteB(nonZeroPtr, directKey, directKey.position(), directKey.remaining())
            } else {
                val arrayKey = key.array()
                Native.writeBatchDeleteA(nonZeroPtr, arrayKey.array, arrayKey.offset, arrayKey.length)
            }
        }

        override fun clear() {
            Native.writeBatchClear(nonZeroPtr)
        }

        override fun append(source: LevelDB.WriteBatch) {
            Native.writeBatchAppend(nonZeroPtr, (source as WriteBatch).nonZeroPtr)
        }

        override fun release(ptr: Long) {
            Native.writeBatchRelease(ptr)
        }
    }


    private class Snapshot internal constructor(private val _dbPtr: Long, ptr: Long, handler: Handler, options: LevelDB.Options) : NativeBound(ptr, "Snapshot", handler, options), LevelDB.Snapshot {

        override fun release(ptr: Long) {
            Native.snapshotRelease(_dbPtr, ptr)
        }

    }


    private class Cursor internal constructor(ptr: Long, handler: Handler, options: LevelDB.Options) : NativeBound(ptr, "Cursor", handler, options), LevelDB.Cursor {

        private val itHandler = Handler()

        internal abstract class AbstractBytesArray(
                name: String,
                // There are probably less buffer pointers than there are entries. All unused slots are set to zero.
                private val ptrs: LongArray,
                // There are probably less buffers than there are entries. All unused slots are set to null.
                protected val buffers: Array<ByteBuffer?>,
                // There might be less entry than this array length. _length should always be used in place of this arrays length.
                protected val indexes: IntArray,
                // There might be less entry than this array length. _length should always be used in place of this arrays length.
                protected val keys: IntArray,
                // There might be less entry than this array length. _length should always be used in place of this arrays length.
                protected val values: IntArray,
                // There might be less entry than this array length. _length should always be used in place of this arrays length.
                protected val limit: IntArray,
                handler: Handler?,
                options: LevelDB.Options
        ) : PlatformCloseable(name, handler, options), LevelDB.Cursor.ValuesArrayBase {

            final override val size: Int

            init {

                // The length (= number of entries) can be found as either the number of indexes values before the first -1, or the length of the indexes array if no -1 is found.
                var i = 0
                while (i < indexes.size) {
                    if (indexes[i] == -1)
                        break
                    ++i
                }
                size = i
            }

            companion object {
//                @JvmStatic private external fun n_Release(ptrs: LongArray)
            }

            override fun getKey(i: Int): KBuffer {
                val index = indexes[i]
                if (index == -1)
                    throw ArrayIndexOutOfBoundsException(i)
                val key = buffers[index]!!.duplicate()
                key.position(keys[i])
                key.limit(values[i])
                return KBuffer.wrap(key.slice())
            }

            override fun getValue(i: Int): KBuffer? {
                val index = indexes[i]
                if (index == -1)
                    throw ArrayIndexOutOfBoundsException(i)
                if (limit[i] == -1)
                    return null
                val value = buffers[index]!!.duplicate()
                value.position(values[i])
                value.limit(limit[i])
                return KBuffer.wrap(value.slice())
            }

            override fun platformClose() {
                Native.iteratorArrayRelease(ptrs)
                Arrays.fill(ptrs, 0)
            }
        }

        internal class BytesArray(ptrs: LongArray, buffers: Array<ByteBuffer?>, indexes: IntArray, keys: IntArray, values: IntArray, limit: IntArray, handler: Handler?, options: LevelDB.Options)
            : AbstractBytesArray("CursorArray", ptrs, buffers, indexes, keys, values, limit, handler, options), LevelDB.Cursor.ValuesArray {

            override fun getValue(i: Int) = super.getValue(i)!!
        }

        internal class IndirectBytesArray(ptrs: LongArray, buffers: Array<ByteBuffer?>, indexes: IntArray, private val indirectKeys: IntArray?, keys: IntArray, values: IntArray, limit: IntArray, handler: Handler?, options: LevelDB.Options)
            : AbstractBytesArray("CursorIndirectArray", ptrs, buffers, indexes, keys, values, limit, handler, options), LevelDB.Cursor.IndirectValuesArray {

            override fun getIntermediateKey(i: Int): KBuffer {
                if (indirectKeys == null)
                    return getKey(i)

                val index = indexes[i]
                if (index == -1)
                    throw ArrayIndexOutOfBoundsException(i)
                val key = buffers[index]!!.duplicate()
                key.position(indirectKeys[i])
                key.limit(keys[i])
                return KBuffer.wrap(key.slice())
            }
        }


        override fun isValid(): Boolean {
            return Native.iteratorValid(nonZeroPtr)
        }

        override fun seekToFirst() {
            Native.iteratorSeekToFirst(nonZeroPtr)
        }

        override fun seekToLast() {
            Native.iteratorSeekToLast(nonZeroPtr)
        }

        override fun seekTo(target: ReadBuffer) {
            val directTarget = target.directByteBuffer()
            if (directTarget != null) {
                Native.iteratorSeekB(nonZeroPtr, directTarget, directTarget.position(), directTarget.remaining())
            } else {
                val arrayTarget = target.array()
                Native.iteratorSeekA(nonZeroPtr, arrayTarget.array, arrayTarget.offset, arrayTarget.length)
            }
        }

        override fun next() {
            Native.iteratorNext(nonZeroPtr)
        }

        override fun nextArray(size: Int, bufferSize: Int): LevelDB.Cursor.ValuesArray {
            val ptrs = LongArray(size)
            val buffers = arrayOfNulls<ByteBuffer>(size)
            val indexes = IntArray(size)
            val keys = IntArray(size)
            val values = IntArray(size)
            val limits = IntArray(size)

            Native.iteratorArrayNext(nonZeroPtr, ptrs, buffers, indexes, keys, values, limits, if (bufferSize == -1) options.defaultCursorArrayBufferSize else bufferSize)

            return BytesArray(ptrs, buffers, indexes, keys, values, limits, itHandler, options)
        }

        override fun nextIndirectArray(db: LevelDB, size: Int, bufferSize: Int, options: LevelDB.ReadOptions): LevelDB.Cursor.IndirectValuesArray {
            val ptrs = LongArray(size)
            val buffers = arrayOfNulls<ByteBuffer>(size)
            val indexes = IntArray(size)
            val intermediateKeys = IntArray(size)
            val keys = IntArray(size)
            val values = IntArray(size)
            val limits = IntArray(size)

            Native.iteratorArrayNextIndirect((db as LevelDBJNI).nonZeroPtr, nonZeroPtr, options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot), ptrs, buffers, indexes, intermediateKeys, keys, values, limits, if (bufferSize == -1) this.options.defaultCursorArrayBufferSize else bufferSize)

            return IndirectBytesArray(ptrs, buffers, indexes, intermediateKeys, keys, values, limits, itHandler, this.options)

        }

        override fun prev() {
            Native.iteratorPrev(nonZeroPtr)
        }

        override fun transientKey(): KBuffer {
            return KBuffer.wrap(Native.iteratorKey(nonZeroPtr))
        }

        override fun transientValue(): KBuffer {
            return KBuffer.wrap(Native.iteratorValue(nonZeroPtr))
        }

        override fun beforeClose() {
            itHandler.close()
        }

        override fun release(ptr: Long) {
            Native.iteratorRelease(ptr)
        }
    }

    companion object {

        private fun newNativeOptions(options: LevelDB.Options): Long {
            return Native.optionsNew(
                    options.printLogs,
                    options.openPolicy.createIfMissing,
                    options.openPolicy.errorIfExists,
                    options.paranoidChecks,
                    options.writeBufferSize,
                    options.maxOpenFiles,
                    options.cacheSize,
                    options.blockSize,
                    options.blockRestartInterval,
                    options.maxFileSize,
                    options.snappyCompression,
                    options.reuseLogs,
                    options.bloomFilterBitsPerKey
            )
        }

        private fun snapshotPtr(snapshot: LevelDB.Snapshot?): Long {
            return if (snapshot != null) (snapshot as Snapshot).nonZeroPtr else 0
        }
    }

}
