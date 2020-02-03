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

//    @JvmStatic external fun iteratorValid(itPtr: Long): Boolean

    @JvmStatic external fun iteratorSeekToFirst(itPtr: Long, lens: IntArray)
    @JvmStatic external fun iteratorSeekToLast(itPtr: Long, lens: IntArray)

    @JvmStatic external fun iteratorSeekB(itPtr: Long, target: ByteBuffer, targetOffset: Int, targetLength: Int, lens: IntArray)
    @JvmStatic external fun iteratorSeekA(itPtr: Long, target: ByteArray, targetOffset: Int, targetLength: Int, lens: IntArray)

    @JvmStatic external fun iteratorNext(itPtr: Long, lens: IntArray)
    @JvmStatic external fun iteratorPrev(itPtr: Long, lens: IntArray)

    @JvmStatic external fun iteratorKey(itPtr: Long, buffer: ByteBuffer)
    @JvmStatic external fun iteratorValue(itPtr: Long, buffer: ByteBuffer)

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
