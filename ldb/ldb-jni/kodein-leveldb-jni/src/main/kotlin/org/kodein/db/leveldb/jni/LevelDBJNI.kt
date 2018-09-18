package org.kodein.db.leveldb.jni

import org.kodein.db.leveldb.*
import java.nio.ByteBuffer
import java.util.*

/**
 * Native implementation of the LevelDB interface, using Google's C++ LevelDB.
 *
 * This is the recommended implementation of [LevelDB] and by far the fastest.
 *
 * However, it requires its native library to be loaded (loading depends on the platform).
 */
class LevelDBJNI private constructor(ptr: Long, val optionsPtr: Long, options: LevelDB.Options) : NativeBound(ptr, "DB", null, options), LevelDB {

    private val dbHandler = PlatformCloseable.Handler()

    /**
     * LevelDB Factory that handles native LevelDB databases.
     */
    object Factory : LevelDB.Factory {

        override fun open(path: String, options: LevelDB.Options): LevelDB {
            val optionsPtr = newNativeOptions(options)
            try {
                return LevelDBJNI(n_OpenDB(path, optionsPtr, options.repairOnCorruption), optionsPtr, options)
            }
            catch (e: Throwable) {
                n_ReleaseOptions(optionsPtr)
                throw e
            }
        }

        override fun destroy(path: String, options: LevelDB.Options) {
            val optionsPtr = newNativeOptions(options)
            try {
                n_DestroyDB(path, optionsPtr)
            }
            finally {
                n_ReleaseOptions(optionsPtr)
            }
        }
    }


    override fun beforeClose() {
        // Before effectively closing the database, we need to close all non-closed related objects.
        dbHandler.close()
    }

    override fun put(key: ByteBuffer, value: ByteBuffer, options: LevelDB.WriteOptions) {
        // Calls the correct native function according to the types of ByteBuffer the key and value are.
        if (key.isDirect && value.isDirect)
            n_Put_BB(nonZeroPtr, key, key.position(), key.remaining(), value, value.position(), value.remaining(), options.sync)
        else if (key.hasArray() && value.isDirect)
            n_Put_AB(nonZeroPtr, key.array(), key.arrayOffset(), key.remaining(), value, value.position(), value.remaining(), options.sync)
        else if (key.isDirect && value.hasArray())
            n_Put_BA(nonZeroPtr, key, key.position(), key.remaining(), value.array(), value.arrayOffset(), value.remaining(), options.sync)
        else if (key.hasArray() && value.hasArray())
            n_Put_AA(nonZeroPtr, key.array(), key.arrayOffset(), key.remaining(), value.array(), value.arrayOffset(), value.remaining(), options.sync)
        else
            throw IllegalStateException("Buffers must be either direct or backed by a byte array")
    }

    override fun put(key: Allocation, value: Allocation, options: LevelDB.WriteOptions) = put(key.toByteBuffer(), value.toByteBuffer(), options)

    override fun delete(key: ByteBuffer, options: LevelDB.WriteOptions) {
        // Calls the correct native function according to the types of ByteBuffer the key is.
        if (key.isDirect)
            n_Delete_B(nonZeroPtr, key, key.position(), key.remaining(), options.sync)
        else if (key.hasArray())
            n_Delete_A(nonZeroPtr, key.array(), key.arrayOffset(), key.remaining(), options.sync)
        else
            throw IllegalStateException("Buffers must be either direct or backed by a byte array")
    }

    override fun delete(key: Allocation, options: LevelDB.WriteOptions) = delete(key.toByteBuffer(), options)

    override fun write(batch: LevelDB.WriteBatch, options: LevelDB.WriteOptions) {
        n_Write(nonZeroPtr, (batch as WriteBatch).nonZeroPtr, options.sync)
    }

    override fun get(key: ByteBuffer, options: LevelDB.ReadOptions): LevelDB.NativeBytes? {
        val valuePtr: Long
        // Calls the correct native function according to the types of ByteBuffer the key is.
        if (key.isDirect)
            valuePtr = n_Get_B(nonZeroPtr, key, key.position(), key.remaining(), options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        else if (key.hasArray())
            valuePtr = n_Get_A(nonZeroPtr, key.array(), key.arrayOffset(), key.remaining(), options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        else
            throw IllegalStateException("Buffers must be either direct or backed by a byte array")

        return if (valuePtr == 0L) null else NativeBytes(valuePtr, dbHandler, this.options)

    }

    override fun get(key: Allocation, options: LevelDB.ReadOptions) = get(key.toByteBuffer(), options)

    override fun indirectGet(key: ByteBuffer, options: LevelDB.ReadOptions): LevelDB.NativeBytes? {
        val valuePtr: Long
        // Calls the correct native function according to the types of ByteBuffer the key is.
        if (key.isDirect)
            valuePtr = n_IndirectGet_B(nonZeroPtr, key, key.position(), key.remaining(), options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        else if (key.hasArray())
            valuePtr = n_IndirectGet_A(nonZeroPtr, key.array(), key.arrayOffset(), key.remaining(), options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        else
            throw IllegalStateException("Buffers must be either direct or backed by a byte array")

        return if (valuePtr == 0L) null else NativeBytes(valuePtr, dbHandler, this.options)
    }

    override fun indirectGet(key: Allocation, options: LevelDB.ReadOptions) = indirectGet(key.toByteBuffer(), options)

    override fun indirectGet(it: LevelDB.Iterator, options: LevelDB.ReadOptions): LevelDB.NativeBytes? {
        val valuePtr = n_IndirectGet_I(nonZeroPtr, (it as Iterator).nonZeroPtr, options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot))
        return if (valuePtr == 0L) null else NativeBytes(valuePtr, dbHandler, this.options)
    }

    override fun newIterator(options: LevelDB.ReadOptions): LevelDB.Iterator {
        return Iterator(n_NewIterator(nonZeroPtr, options.verifyChecksums, options.fillCache, snapshotPtr(options.snapshot)), dbHandler, this.options)
    }

    override fun newSnapshot(): LevelDB.Snapshot {
        val ptr = nonZeroPtr
        return Snapshot(ptr, n_NewSnapshot(ptr), dbHandler, options)
    }

    override fun newWriteBatch(): LevelDB.WriteBatch {
        return WriteBatch(n_NewWriteBatch(), dbHandler, options)
    }

    override fun release(ptr: Long) {
        n_Release(ptr)
        n_ReleaseOptions(optionsPtr)
    }


    private class NativeBytes internal constructor(ptr: Long, handler: PlatformCloseable.Handler, options: LevelDB.Options) : NativeBound(ptr, "Value", handler, options), LevelDB.NativeBytes {

        override val allocation: Allocation = Allocation(n_Buffer(ptr), true)
            get() {
                checkIsOpen()
                return field
            }

        companion object {
            @JvmStatic private external fun n_Buffer(ptr: Long): ByteBuffer

            @JvmStatic private external fun n_Release(ptr: Long)
        }

        override fun release(ptr: Long) {
            n_Release(ptr)
        }
    }


    private class WriteBatch internal constructor(ptr: Long, handler: PlatformCloseable.Handler, options: LevelDB.Options) : NativeBound(ptr, "WriteBatch", handler, options), LevelDB.WriteBatch {

        companion object {
            @JvmStatic private external fun n_Put_BB(ptr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, body: ByteBuffer, bodyOffset: Int, bodyLength: Int)
            @JvmStatic private external fun n_Put_AB(ptr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, body: ByteBuffer, bodyOffset: Int, bodyLength: Int)
            @JvmStatic private external fun n_Put_BA(ptr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, body: ByteArray, bodyOffset: Int, bodyLength: Int)
            @JvmStatic private external fun n_Put_AA(ptr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, body: ByteArray, bodyOffset: Int, bodyLength: Int)

            @JvmStatic private external fun n_Delete_B(ptr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int)

            @JvmStatic private external fun n_Delete_A(ptr: Long, key: ByteArray, keyOffset: Int, keyLength: Int)

            @JvmStatic private external fun n_Release(ptr: Long)
        }

        override fun put(key: ByteBuffer, value: ByteBuffer) {
            // Calls the correct native function according to the types of ByteBuffer the key and value are.
            if (key.isDirect && value.isDirect)
                n_Put_BB(nonZeroPtr, key, key.position(), key.remaining(), value, value.position(), value.remaining())
            else if (key.hasArray() && value.isDirect)
                n_Put_AB(nonZeroPtr, key.array(), key.arrayOffset(), key.remaining(), value, value.position(), value.remaining())
            else if (key.isDirect && value.hasArray())
                n_Put_BA(nonZeroPtr, key, key.position(), key.remaining(), value.array(), value.arrayOffset(), value.remaining())
            else if (key.hasArray() && value.hasArray())
                n_Put_AA(nonZeroPtr, key.array(), key.arrayOffset(), key.remaining(), value.array(), value.arrayOffset(), value.remaining())
            else
                throw IllegalStateException("Buffers must be either direct or backed by a byte array")
        }

        override fun put(key: Allocation, value: Allocation) = put(key.toByteBuffer(), value.toByteBuffer())

        override fun delete(key: ByteBuffer) {
            if (key.isDirect)
                n_Delete_B(nonZeroPtr, key, key.position(), key.remaining())
            else if (key.hasArray())
                n_Delete_A(nonZeroPtr, key.array(), key.arrayOffset(), key.remaining())
            else
                throw IllegalStateException("Buffers must be either direct or backed by a byte array")
        }

        override fun delete(key: Allocation) = delete(key.toByteBuffer())

        override fun release(ptr: Long) {
            n_Release(ptr)
        }
    }


    private class Snapshot internal constructor(private val _dbPtr: Long, ptr: Long, handler: PlatformCloseable.Handler, options: LevelDB.Options) : NativeBound(ptr, "Snapshot", handler, options), LevelDB.Snapshot {

        companion object {
            @JvmStatic private external fun n_Release(dbPtr: Long, snapshotPtr: Long)
        }

        override fun release(ptr: Long) {
            n_Release(_dbPtr, ptr)
        }

    }


    private class Iterator internal constructor(ptr: Long, handler: PlatformCloseable.Handler, options: LevelDB.Options) : NativeBound(ptr, "Iterator", handler, options), LevelDB.Iterator {

        companion object {
            @JvmStatic private external fun n_Valid(ptr: Long): Boolean

            @JvmStatic private external fun n_SeekToFirst(ptr: Long)
            @JvmStatic private external fun n_SeekToLast(ptr: Long)

            @JvmStatic private external fun n_Seek_B(ptr: Long, target: ByteBuffer, targetOffset: Int, targetLength: Int)
            @JvmStatic private external fun n_Seek_A(ptr: Long, target: ByteArray, targetOffset: Int, targetLength: Int)

            @JvmStatic private external fun n_Next(ptr: Long)

            @JvmStatic private external fun n_Prev(ptr: Long)

            @JvmStatic private external fun n_key(ptr: Long): ByteBuffer
            @JvmStatic private external fun n_value(ptr: Long): ByteBuffer

            // Get an array of the next entries and move the native iterator to the entry after the last one in the returned array.
            //
            // The point of doing this is optimisation: it enables only one JNI access to fecth a large set of entries, thus limiting JNI access and allowing meaningful JIT optimisation by the JVM.
            //
            // This function will create as little byte buffers as possible.
            // Each byte buffer will have a memory range allocated with the size of bufferSize.
            // If bufferSize is big enough, this means that there should be a lot less byte buffers than there are results as each byte buffers should contain many results, and therefore a lot less GC.
            // However, the bigger the bufferSize, the less GC, but the smallest bufferSize, the less unused memory and therefore a better memory footprint.
            // Note that a bigger memory allocation then bufferSize can happen if it is needed to contain a single entry that's biggest than bufferSize.
            //
            // Each entry is defined by:
            //
            //  - an index that defines in which byte buffer its memory is located
            //  - a key offset that defines the starting position of the key inside the memory.
            //  - a value offset that defines the exclusive end of the key as well as the starting position of the value inside the memory.
            //  - a limit offset that defines the exclusive end of the value inside the memory.
            //
            // All arrays provided to this functions must have a length superior or equal to the length of the indexes array.
            //
            // If there is less entries left in the provided iterator than there are slots in the arrays, the first unused slot in the indexes array will be set to -1.
            @JvmStatic private external fun n_NextArray(ptr: Long, ptrs: LongArray, buffers: Array<ByteBuffer?>, indexes: IntArray, keys: IntArray, values: IntArray, limits: IntArray, bufferSize: Int)

            @JvmStatic private external fun n_Release(ptr: Long)
        }


        private class NativeBytesArray(
                // There are probably less buffer pointers than there are entries. All unused slots are set to zero.
                private val ptrs: LongArray,
                // There are probably less buffers than there are entries. All unused slots are set to null.
                private val buffers: Array<ByteBuffer?>,
                // There might be less entry than this array length. _length should always be used in place of this arrays length.
                private val indexes: IntArray,
                // There might be less entry than this array length. _length should always be used in place of this arrays length.
                private val keys: IntArray,
                // There might be less entry than this array length. _length should always be used in place of this arrays length.
                private val values: IntArray,
                // There might be less entry than this array length. _length should always be used in place of this arrays length.
                private val limit: IntArray,
                handler: PlatformCloseable.Handler?,
                options: LevelDB.Options
        ) : PlatformCloseable("IteratorArray", handler, options), LevelDB.Iterator.NativeBytesArray {

            override val size: Int

            override val isClosed: Boolean
                get() = ptrs[0] == 0L

            init {

                // The length (= number of entries) can be found as either the number of indexes values before the first -1, or the length of the indexes array if no -1 is found.
                var i: Int
                i = 0
                while (i < indexes.size) {
                    if (indexes[i] == -1)
                        break
                    ++i
                }
                size = i
            }

            companion object {
                @JvmStatic private external fun n_Release(ptrs: LongArray)
            }

            override fun getKey(i: Int): Allocation {
                val index = indexes[i]
                if (index == -1)
                    throw ArrayIndexOutOfBoundsException(i)
                val key = buffers[index]!!.duplicate()
                key.position(keys[i])
                key.limit(values[i])
                return Allocation(key.slice(), true)
            }

            override fun getValue(i: Int): Allocation {
                val index = indexes[i]
                if (index == -1)
                    throw ArrayIndexOutOfBoundsException(i)
                val value = buffers[index]!!.duplicate()
                value.position(values[i])
                value.limit(limit[i])
                return Allocation(value.slice(), true)
            }

            override fun platformClose() {
                n_Release(ptrs)
                Arrays.fill(ptrs, 0)
            }
        }


        override fun isValid(): Boolean {
            return n_Valid(nonZeroPtr)
        }

        override fun seekToFirst() {
            n_SeekToFirst(nonZeroPtr)
        }

        override fun seekToLast() {
            n_SeekToLast(nonZeroPtr)
        }

        override fun seekTo(target: ByteBuffer) {
            if (target.isDirect)
                n_Seek_B(nonZeroPtr, target, target.position(), target.remaining())
            else if (target.hasArray())
                n_Seek_A(nonZeroPtr, target.array(), target.arrayOffset(), target.remaining())
            else
                throw IllegalStateException("Buffers must be either direct or backed by a byte array")
        }

        override fun seekTo(target: Allocation) = seekTo(target.toByteBuffer())

        override fun next() {
            n_Next(nonZeroPtr)
        }

        override fun nextArray(size: Int, bufferSize: Int): LevelDB.Iterator.NativeBytesArray {
            val ptrs = LongArray(size)
            val buffers = arrayOfNulls<ByteBuffer>(size)
            val indexes = IntArray(size)
            val keys = IntArray(size)
            val values = IntArray(size)
            val limits = IntArray(size)

            n_NextArray(nonZeroPtr, ptrs, buffers, indexes, keys, values, limits, if (bufferSize == -1) options.defaultIteratorArrayBufferSize else bufferSize)

            return NativeBytesArray(ptrs, buffers, indexes, keys, values, limits, handler, options)
        }

        override fun prev() {
            n_Prev(nonZeroPtr)
        }

        override fun transientKey(): Allocation {
            return Allocation(n_key(nonZeroPtr), true)
        }

        override fun transientValue(): Allocation {
            return Allocation(n_value(nonZeroPtr), true)
        }

        override fun release(ptr: Long) {
            n_Release(ptr)
        }
    }

    companion object {

        @JvmStatic private external fun n_NewOptions(
                printLogs: Boolean,
                createIfMissing: Boolean,
                errorIfExists: Boolean,
                paranoidChecks: Boolean,
                writeBufferSize: Int,
                maxOpenFiles: Int,
                cacheSize: Int,
                blockSize: Int,
                blockRestartInterval: Int,
                maxFileSize: Int,
                snappyCompression: Boolean,
                bloomFilterBitsPerKey: Int
        ): Long

        @JvmStatic private external fun n_ReleaseOptions(optionsPtr: Long)

        @JvmStatic private external fun n_OpenDB(path: String, optionsPtr: Long, repairOnCorruption: Boolean): Long

        @JvmStatic private external fun n_DestroyDB(path: String, optionsPtr: Long)

        @JvmStatic private external fun n_Put_BB(ptr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, body: ByteBuffer, bodyOffset: Int, bodyLength: Int, sync: Boolean)
        @JvmStatic private external fun n_Put_AB(ptr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, body: ByteBuffer, bodyOffset: Int, bodyLength: Int, sync: Boolean)
        @JvmStatic private external fun n_Put_BA(ptr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, body: ByteArray, bodyOffset: Int, bodyLength: Int, sync: Boolean)
        @JvmStatic private external fun n_Put_AA(ptr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, body: ByteArray, bodyOffset: Int, bodyLength: Int, sync: Boolean)

        @JvmStatic private external fun n_Delete_B(ptr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, sync: Boolean)
        @JvmStatic private external fun n_Delete_A(ptr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, sync: Boolean)

        @JvmStatic private external fun n_Write(ptr: Long, batchPtr: Long, sync: Boolean)

        @JvmStatic private external fun n_Get_B(ptr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long
        @JvmStatic private external fun n_Get_A(ptr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long

        @JvmStatic private external fun n_IndirectGet_B(ptr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long
        @JvmStatic private external fun n_IndirectGet_A(ptr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long
        @JvmStatic private external fun n_IndirectGet_I(ptr: Long, iteratorPtr: Long, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long

        @JvmStatic private external fun n_NewIterator(ptr: Long, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long

        @JvmStatic private external fun n_NewSnapshot(ptr: Long): Long

        @JvmStatic private external fun n_NewWriteBatch(): Long

        @JvmStatic private external fun n_Release(ptr: Long)

        private fun newNativeOptions(options: LevelDB.Options): Long {
            return n_NewOptions(
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
                    options.bloomFilterBitsPerKey
            )
        }

        private fun snapshotPtr(snapshot: LevelDB.Snapshot?): Long {
            return if (snapshot != null) (snapshot as Snapshot).nonZeroPtr else 0
        }
    }

}
