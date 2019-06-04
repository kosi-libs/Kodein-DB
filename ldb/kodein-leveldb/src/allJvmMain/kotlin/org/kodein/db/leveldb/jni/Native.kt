package org.kodein.db.leveldb.jni

import java.nio.ByteBuffer

internal object Native {

    @JvmStatic external fun bufferNew(ptr: Long): ByteBuffer
    @JvmStatic external fun bufferRelease(ptr: Long)

    @JvmStatic external fun optionsNew(
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
            reuseLogs: Boolean,
            bloomFilterBitsPerKey: Int
    ): Long

    @JvmStatic external fun optionsRelease(optionsPtr: Long)

    @JvmStatic external fun dbOpen(path: String, optionsPtr: Long, repairOnCorruption: Boolean): Long
    @JvmStatic external fun dbRelease(dbPtr: Long)

    @JvmStatic external fun dbDestroy(path: String, optionsPtr: Long)

    @JvmStatic external fun putBB(dbPtr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, body: ByteBuffer, bodyOffset: Int, bodyLength: Int, sync: Boolean)
    @JvmStatic external fun putAB(dbPtr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, body: ByteBuffer, bodyOffset: Int, bodyLength: Int, sync: Boolean)
    @JvmStatic external fun putBA(dbPtr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, body: ByteArray, bodyOffset: Int, bodyLength: Int, sync: Boolean)
    @JvmStatic external fun putAA(dbPtr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, body: ByteArray, bodyOffset: Int, bodyLength: Int, sync: Boolean)

    @JvmStatic external fun deleteB(dbPtr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, sync: Boolean)
    @JvmStatic external fun deleteA(dbPtr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, sync: Boolean)

    @JvmStatic external fun write(dbPtr: Long, batchPtr: Long, sync: Boolean)

    @JvmStatic external fun getB(dbPtr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long
    @JvmStatic external fun getA(dbPtr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long

    @JvmStatic external fun indirectGetB(dbPtr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long
    @JvmStatic external fun indirectGetA(dbPtr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long
    @JvmStatic external fun indirectGetI(dbPtr: Long, iteratorPtr: Long, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long

    @JvmStatic external fun iteratorNew(dbPtr: Long, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long): Long
    @JvmStatic external fun iteratorRelease(itPtr: Long)

    @JvmStatic external fun iteratorValid(itPtr: Long): Boolean

    @JvmStatic external fun iteratorSeekToFirst(itPtr: Long)
    @JvmStatic external fun iteratorSeekToLast(itPtr: Long)

    @JvmStatic external fun iteratorSeekB(itPtr: Long, target: ByteBuffer, targetOffset: Int, targetLength: Int)
    @JvmStatic external fun iteratorSeekA(itPtr: Long, target: ByteArray, targetOffset: Int, targetLength: Int)

    @JvmStatic external fun iteratorNext(itPtr: Long)
    @JvmStatic external fun iteratorPrev(itPtr: Long)

    @JvmStatic external fun iteratorKey(itPtr: Long): ByteBuffer
    @JvmStatic external fun iteratorValue(itPtr: Long): ByteBuffer

    // Get an array of the next entries and move the native cursor to the entry after the last one in the returned array.
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
    // If there is less entries left in the provided curfsor than there are slots in the arrays, the first unused slot in the indexes array will be set to -1.
    @JvmStatic external fun iteratorArrayNext(itPtr: Long, ptrs: LongArray, buffers: Array<ByteBuffer?>, indexes: IntArray, keys: IntArray, values: IntArray, limits: IntArray, bufferSize: Int)

    @JvmStatic external fun iteratorArrayNextIndirect(dbPtr: Long, itPtr: Long, verifyChecksum: Boolean, fillCache: Boolean, snapshotPtr: Long, ptrs: LongArray, buffers: Array<ByteBuffer?>, indexes: IntArray, intermediateKeys: IntArray, keys: IntArray, values: IntArray, limits: IntArray, bufferSize: Int)

    @JvmStatic external fun iteratorArrayRelease(ptrs: LongArray)

    @JvmStatic external fun snapshotNew(dbPtr: Long): Long
    @JvmStatic external fun snapshotRelease(dbPtr: Long, snapshotPtr: Long)

    @JvmStatic external fun writeBatchNew(): Long
    @JvmStatic external fun writeBatchRelease(wbPtr: Long)

    @JvmStatic external fun writeBatchPutBB(wbPtr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, body: ByteBuffer, bodyOffset: Int, bodyLength: Int)
    @JvmStatic external fun writeBatchPutAB(wbPtr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, body: ByteBuffer, bodyOffset: Int, bodyLength: Int)
    @JvmStatic external fun writeBatchPutBA(wbPtr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int, body: ByteArray, bodyOffset: Int, bodyLength: Int)
    @JvmStatic external fun writeBatchPutAA(wbPtr: Long, key: ByteArray, keyOffset: Int, keyLength: Int, body: ByteArray, bodyOffset: Int, bodyLength: Int)

    @JvmStatic external fun writeBatchDeleteB(wbPtr: Long, key: ByteBuffer, keyOffset: Int, keyLength: Int)
    @JvmStatic external fun writeBatchDeleteA(wbPtr: Long, key: ByteArray, keyOffset: Int, keyLength: Int)

    @JvmStatic external fun writeBatchClear(wbPtr: Long)

    @JvmStatic external fun writeBatchAppend(wbPtr: Long, sourcePtr: Long)

}
