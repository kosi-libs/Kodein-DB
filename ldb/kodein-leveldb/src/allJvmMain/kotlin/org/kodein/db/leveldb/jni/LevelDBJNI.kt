package org.kodein.db.leveldb.jni

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBException
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.memory.io.*
import java.nio.ByteBuffer

/**
 * Native implementation of the LevelDB interface, using Google's C++ LevelDB.
 *
 * This is the recommended implementation of [LevelDB] and by far the fastest.
 *
 * However, it requires its native library to be loaded (loading depends on the platform).
 */
@Suppress("FunctionName")
public class LevelDBJNI private constructor(ptr: Long, private val optionsPtr: Long, options: LevelDB.Options, override val path: String) : NativeBound(ptr, "DB", null, options), LevelDB {

    private val dbHandler = Handler()

    /**
     * LevelDB Factory that handles native LevelDB databases.
     */
    public object Factory : LevelDBFactory {

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

    override fun put(key: ReadMemory, value: ReadMemory, options: LevelDB.WriteOptions) {
        val directKey = key.directByteBuffer()
        val directValue = value.directByteBuffer()

        if (directKey != null && directValue != null) {
            Native.putBB(nonZeroPtr, directKey, directKey.limit(), directValue, directValue.limit(), options.sync)
        } else if (directValue != null) {
            Native.putAB(nonZeroPtr, key.array(), key.arrayOffset(), key.size, directValue, directValue.limit(), options.sync)
        } else if (directKey != null) {
            Native.putBA(nonZeroPtr, directKey, directKey.limit(), value.array(), value.arrayOffset(), value.size, options.sync)
        } else {
            Native.putAA(nonZeroPtr, key.array(), key.arrayOffset(), key.size, value.array(), value.arrayOffset(), value.size, options.sync)
        }
    }

    override fun delete(key: ReadMemory, options: LevelDB.WriteOptions) {
        val directKey = key.directByteBuffer()
        if (directKey != null) {
            Native.deleteB(nonZeroPtr, directKey, directKey.limit(), options.sync)
        } else {
            Native.deleteA(nonZeroPtr, key.array(), key.arrayOffset(), key.size, options.sync)
        }
    }

    override fun write(batch: LevelDB.WriteBatch, options: LevelDB.WriteOptions) {
        Native.write(nonZeroPtr, (batch as WriteBatch).nonZeroPtr, options.sync)
    }

    override fun get(key: ReadMemory, options: LevelDB.ReadOptions): Allocation? {
        val directKey = key.directByteBuffer()
        val valuePtr = if (directKey != null) {
            Native.getB(nonZeroPtr, directKey, directKey.limit(), options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        } else {
            Native.getA(nonZeroPtr, key.array(), key.arrayOffset(), key.size, options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        }
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

    private class NativeBytes constructor(ptr: Long, handler: Handler, options: LevelDB.Options, override val memory: Memory = Memory.wrap(Native.bufferNew(ptr)))
        : NativeBound(ptr, "Value", handler, options), Allocation, Memory by memory {

        override fun release(ptr: Long) {
            Native.bufferRelease(ptr)
        }

        override fun toString(): String = memory.toString()
    }

    private class WriteBatch constructor(ptr: Long, handler: Handler, options: LevelDB.Options) : NativeBound(ptr, "WriteBatch", handler, options), LevelDB.WriteBatch {

        override fun put(key: ReadMemory, value: ReadMemory) {
            val directKey = key.directByteBuffer()
            val directValue = value.directByteBuffer()

            if (directKey != null && directValue != null) {
                Native.writeBatchPutBB(nonZeroPtr, directKey, directKey.limit(), directValue, directValue.limit())
            } else if (directValue != null) {
                Native.writeBatchPutAB(nonZeroPtr, key.array(), key.arrayOffset(), key.size, directValue, directValue.limit())
            } else if (directKey != null) {
                Native.writeBatchPutBA(nonZeroPtr, directKey, directKey.limit(), value.array(), value.arrayOffset(), value.size)
            } else {
                Native.writeBatchPutAA(nonZeroPtr, key.array(), key.arrayOffset(), key.size, value.array(), value.arrayOffset(), value.size)
            }
        }

        override fun delete(key: ReadMemory) {
            val directKey = key.directByteBuffer()
            if (directKey != null) {
                Native.writeBatchDeleteB(nonZeroPtr, directKey, directKey.limit())
            } else {
                Native.writeBatchDeleteA(nonZeroPtr, key.array(), key.arrayOffset(), key.size)
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


    private class Snapshot constructor(private val _dbPtr: Long, ptr: Long, handler: Handler, options: LevelDB.Options) : NativeBound(ptr, "Snapshot", handler, options), LevelDB.Snapshot {

        override fun release(ptr: Long) {
            Native.snapshotRelease(_dbPtr, ptr)
        }

    }

    private class Cursor constructor(ptr: Long, handler: Handler, options: LevelDB.Options) : NativeBound(ptr, "Cursor", handler, options), LevelDB.Cursor {

        private val lens = IntArray(2) { -1 }

        override fun isValid(): Boolean {
            return lens[0] >= 0
        }

        override fun seekToFirst() {
            Native.iteratorSeekToFirst(nonZeroPtr, lens)
        }

        override fun seekToLast() {
            Native.iteratorSeekToLast(nonZeroPtr, lens)
        }

        override fun seekTo(target: ReadMemory) {
            val directTarget = target.directByteBuffer()
            if (directTarget != null) {
                Native.iteratorSeekB(nonZeroPtr, directTarget, directTarget.limit(), lens)
            } else {
                Native.iteratorSeekA(nonZeroPtr, target.array(), target.arrayOffset(), target.size, lens)
            }
        }

        override fun next() {
            Native.iteratorNext(nonZeroPtr, lens)
        }

        override fun prev() {
            Native.iteratorPrev(nonZeroPtr, lens)
        }

        override fun transientKey(): ReadMemory =
            Memory.wrap(Native.iteratorKey(nonZeroPtr))

        override fun transientValue(): ReadMemory =
            Memory.wrap(Native.iteratorValue(nonZeroPtr))

        override fun release(ptr: Long) {
            Native.iteratorRelease(ptr)
        }
    }

    public companion object {

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
