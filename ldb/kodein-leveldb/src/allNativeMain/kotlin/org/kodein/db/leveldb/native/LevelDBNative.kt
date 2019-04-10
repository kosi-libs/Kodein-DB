package org.kodein.db.leveldb.native

import cnames.structs.leveldb_iterator_t
import cnames.structs.leveldb_options_t
import cnames.structs.leveldb_readoptions_t
import cnames.structs.leveldb_snapshot_t
import cnames.structs.leveldb_t
import cnames.structs.leveldb_writebatch_t
import cnames.structs.leveldb_writeoptions_t
import cnames.structs.leveldb_cache_t
import cnames.structs.leveldb_filterpolicy_t
import platform.posix.size_tVar
import kotlinx.cinterop.*
import kotlinx.io.core.*
import org.kodein.db.leveldb.*
import org.kodein.db.libleveldb.*

private inline fun <T> ldbCall(crossinline block: MemScope.(CPointerVar<ByteVar>) -> T): T = memScoped {
    val errorPtr = allocPointerTo<ByteVar>()
    val ret = block(errorPtr)
    errorPtr.value?.let { error ->
        val errorStr = error.toKString()
//        nativeHeap.free(errorPtr)
        throw LevelDBException(errorStr)
    }
    ret
}

class OptionsPtrs(val options: CPointer<leveldb_options_t>, val cache: CPointer<leveldb_cache_t>?, val filterPolicy: CPointer<leveldb_filterpolicy_t>?)

private fun LevelDB.Options.allocOptionsPtr(): OptionsPtrs {

    val ptrs = OptionsPtrs(
            leveldb_options_create()!!,
            if (cacheSize > 0) leveldb_cache_create_lru(cacheSize.convert()) else null,
            if (bloomFilterBitsPerKey > 0) leveldb_filterpolicy_create_bloom(bloomFilterBitsPerKey) else null
    )

    leveldb_options_set_info_log(ptrs.options, null)
    leveldb_options_set_create_if_missing(ptrs.options, openPolicy.createIfMissing.toByte().toUByte())
    leveldb_options_set_error_if_exists(ptrs.options, openPolicy.errorIfExists.toByte().toUByte())
    leveldb_options_set_paranoid_checks(ptrs.options, paranoidChecks.toByte().toUByte())
    leveldb_options_set_write_buffer_size(ptrs.options, writeBufferSize.convert())
    leveldb_options_set_max_open_files(ptrs.options, maxOpenFiles)
    leveldb_options_set_cache(ptrs.options, ptrs.cache)
    leveldb_options_set_block_size(ptrs.options, blockSize.convert())
    leveldb_options_set_block_restart_interval(ptrs.options, blockRestartInterval)
    leveldb_options_set_compression(ptrs.options, if (snappyCompression) leveldb_snappy_compression.toInt() else leveldb_no_compression.toInt())
    // TODO
//    leveldb_options_set_reuse_logs(ptrs.options, reuseLogs.toByte().toUByte())
    leveldb_options_set_filter_policy(ptrs.options, if (bloomFilterBitsPerKey == 0) null else ptrs.filterPolicy)
    return ptrs
}

private fun releaseOptionsPtr(ptrs: OptionsPtrs) {
    leveldb_options_destroy(ptrs.options)
    ptrs.cache?.let { leveldb_cache_destroy(it) }
    ptrs.filterPolicy?.let { leveldb_filterpolicy_destroy(it) }
}

private fun LevelDB.ReadOptions.allocOptionsPtr(): CPointer<leveldb_readoptions_t> {
    val optionsPtr = leveldb_readoptions_create()!!
    leveldb_readoptions_set_fill_cache(optionsPtr, fillCache.toByte().toUByte())
    leveldb_readoptions_set_verify_checksums(optionsPtr, verifyChecksums.toByte().toUByte())
    leveldb_readoptions_set_snapshot(optionsPtr, (snapshot as LevelDBNative.Snapshot?)?.nonNullPtr)
    return optionsPtr
}

private inline fun LevelDB.ReadOptions.usePointer(block: (CPointer<leveldb_readoptions_t>) -> Unit) {
//    kotlin.contracts.contract {
//        callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
//    }
    val ptr = allocOptionsPtr()
    try {
        block(ptr)
    }
    finally {
        leveldb_readoptions_destroy(ptr)
    }
}

private fun LevelDB.WriteOptions.allocOptionsPtr(): CPointer<leveldb_writeoptions_t> {
    val optionsPtr = leveldb_writeoptions_create()!!
    leveldb_writeoptions_set_sync(optionsPtr, sync.toByte().toUByte())
    return optionsPtr
}

private inline fun LevelDB.WriteOptions.usePointer(block: (CPointer<leveldb_writeoptions_t>) -> Unit) {
    val ptr = allocOptionsPtr()
    try {
        block(ptr)
    }
    finally {
        leveldb_writeoptions_destroy(ptr)
    }
}


class LevelDBNative private constructor(ptr: CPointer<leveldb_t>, options: LevelDB.Options, val optionsPtrs: OptionsPtrs, override val path: String) : PointerBound<leveldb_t>(ptr, "DB", null, options), LevelDB {

    private val dbHandler = PlatformCloseable.Handler()

    object Factory : LevelDB.Factory {

        override fun open(path: String, options: LevelDB.Options): LevelDB {
            val ptrs = options.allocOptionsPtr()
            try {
                val dbPtr = ldbCall { leveldb_open(ptrs.options, path, it.ptr) } ?: throw LevelDBException("Unknown error")
                return LevelDBNative(dbPtr, options, ptrs, path)
            }
            catch (e: Throwable) {
                releaseOptionsPtr(ptrs)
                throw e
            }
        }

        override fun destroy(path: String, options: LevelDB.Options) {
            val ptrs = options.allocOptionsPtr()
            try {
                ldbCall { leveldb_destroy_db(ptrs.options, path, it.ptr) }
            }
            finally {
                releaseOptionsPtr(ptrs)
            }
        }
    }

    override fun put(key: Bytes, value: Bytes, options: LevelDB.WriteOptions) {
        options.usePointer { optionsPtr ->
            ldbCall { leveldb_put(nonNullPtr, optionsPtr, key.content, key.buffer.readRemaining.convert(), value.content, value.buffer.readRemaining.convert(), it.ptr) }
        }
    }

    override fun delete(key: Bytes, options: LevelDB.WriteOptions) {
        options.usePointer { optionsPtr ->
            ldbCall { leveldb_delete(nonNullPtr, optionsPtr, key.content, key.buffer.readRemaining.convert(), it.ptr) }
        }
    }

    override fun write(batch: LevelDB.WriteBatch, options: LevelDB.WriteOptions) {
        options.usePointer { optionsPtr ->
            ldbCall { leveldb_write(nonNullPtr, optionsPtr, (batch as WriteBatch).nonNullPtr, it.ptr) }
        }
    }

    internal class NativeBytes(ptr: CPointer<ByteVar>, len: Int, handler: PlatformCloseable.Handler, options: LevelDB.Options) : PointerBound<ByteVar>(ptr, "Value", handler, options), Allocation {

        private val allocation: Allocation = CPointerAllocation(ptr, len, true)
            get() {
                checkIsOpen()
                return field
            }

        override val buffer get() = allocation.buffer

        override val content get() = allocation.content

        override fun makeView() = allocation.makeView()

        override fun release(ptr: CPointer<ByteVar>) {
            nativeHeap.free(ptr)
        }
    }

    override fun get(key: Bytes, options: LevelDB.ReadOptions): Allocation? {
        options.usePointer { optionsPtr ->
            return ldbCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_get(nonNullPtr, optionsPtr, key.content, key.buffer.readRemaining.convert(), valueSize.ptr, it.ptr)
                if (value != null)
                    NativeBytes(value, valueSize.value.convert(), dbHandler, this@LevelDBNative.options)
                else
                    null

            }
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    override fun indirectGet(key: Bytes, options: LevelDB.ReadOptions): Allocation? {
        options.usePointer { optionsPtr ->
            val (newKey, newKeySize) = ldbCall {
                val newKeySize = alloc<size_tVar>()
                val newKey = leveldb_get(nonNullPtr, optionsPtr, key.content, key.buffer.readRemaining.convert(), newKeySize.ptr, it.ptr)
                newKey to newKeySize.value
            }
            if (newKey == null)
                return null
            return ldbCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_get(nonNullPtr, optionsPtr, newKey, newKeySize, valueSize.ptr, it.ptr)
                if (value != null)
                    NativeBytes(value, valueSize.value.convert(), dbHandler, this@LevelDBNative.options)
                else
                    null
            }
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    override fun indirectGet(cursor: LevelDB.Cursor, options: LevelDB.ReadOptions): Allocation? {
        (cursor as Cursor).checkValid()

        val newKey = cursor.transientValue()

        options.usePointer { optionsPtr ->
            return ldbCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_get(nonNullPtr, optionsPtr, newKey.content, newKey.buffer.readRemaining.convert(), valueSize.ptr, it.ptr)
                if (value != null)
                    NativeBytes(value, valueSize.value.convert(), dbHandler, this@LevelDBNative.options)
                else
                    null
            }
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    internal class Cursor internal constructor(val ldb: LevelDB, ptr: CPointer<leveldb_iterator_t>, handler: PlatformCloseable.Handler, options: LevelDB.Options) : PointerBound<leveldb_iterator_t>(ptr, "Cursor", handler, options), LevelDB.Cursor {

        internal fun checkValid() {
            if (!isValid())
                throw LevelDBException("Cursor is not valid")
        }

        private inline fun <T> ldbItCall(block: MemScope.() -> T): T = memScoped {
            val ret = block()
            val errorPtr = allocPointerTo<ByteVar>()
            leveldb_iter_get_error(nonNullPtr, errorPtr.ptr)
            errorPtr.value?.let { throw LevelDBException(it.toKString()) }
            return ret
        }

        override fun isValid(): Boolean {
            return leveldb_iter_valid(nonNullPtr).toByte().toBoolean()
        }

        override fun seekToFirst() {
            ldbItCall { leveldb_iter_seek_to_first(nonNullPtr) }
        }

        override fun seekToLast() {
            ldbItCall { leveldb_iter_seek_to_last(nonNullPtr) }
        }

        override fun seekTo(target: Bytes) {
            ldbItCall { leveldb_iter_seek(nonNullPtr, target.content, target.buffer.readRemaining.convert()) }
        }

        override fun next() {
            checkValid()
            ldbItCall { leveldb_iter_next(nonNullPtr) }
        }

        override fun prev() {
            checkValid()
            ldbItCall { leveldb_iter_prev(nonNullPtr) }
        }

        internal abstract class NativeBytesArrayBase(
                val name: String,
                val ldb: LevelDB,
                val keys: Array<Allocation?>,
                val values: Array<Allocation?>,
                override val size: Int,
                handler: PlatformCloseable.Handler?,
                options: LevelDB.Options
        ) : PlatformCloseable(name, handler, options), LevelDB.Cursor.ValuesArrayBase {

            internal fun checkIndex(i: Int) {
                checkIsOpen()
                if (i > size)
                    throw ArrayIndexOutOfBoundsException("Index $i is over array size: $size")
            }

            override fun getKey(i: Int): Bytes {
                checkIndex(i)
                return keys[i]!!
            }

            override fun getValue(i: Int): Bytes? {
                checkIndex(i)
                return values[i]
            }

            override fun platformClose() {
                keys.filterNotNull().forEach { it.close() }
                values.filterNotNull().forEach { it.close() }
            }

        }

        internal class NativeBytesArray(ldb: LevelDB, keys: Array<Allocation?>, values: Array<Allocation?>, size: Int, handler: PlatformCloseable.Handler?, options: LevelDB.Options)
            : NativeBytesArrayBase("CursorArray", ldb, keys, values, size, handler, options), LevelDB.Cursor.ValuesArray {

            override fun getValue(i: Int) = super.getValue(i)!!
        }

        internal class NativeIndirectBytesArray(ldb: LevelDB, keys: Array<Allocation?>, val intermediateKeys: Array<Allocation?>, values: Array<Allocation?>, size: Int, handler: PlatformCloseable.Handler?, options: LevelDB.Options)
            : NativeBytesArrayBase("CursorArray", ldb, keys, values, size, handler, options), LevelDB.Cursor.IndirectValuesArray {

            override fun getIntermediateKey(i: Int): Bytes {
                checkIndex(i)
                return intermediateKeys[i]!!
            }

            override fun platformClose() {
                super.platformClose()
                intermediateKeys.filterNotNull().forEach { it.close() }
            }
        }

        override fun nextArray(size: Int, bufferSize: Int): LevelDB.Cursor.ValuesArray {
            val keyArray = arrayOfNulls<Allocation>(size)
            val valueArray = arrayOfNulls<Allocation>(size)
            try {
                var count = 0
                for (i in 0 until size) {
                    if (!isValid())
                        break
                    ++count
                    val key = transientKey()
                    val value = transientValue()
                    keyArray[i] = Allocation.allocNativeBuffer(key.buffer.readRemaining).apply { buffer.writeFully(key.buffer) }
                    valueArray[i] = Allocation.allocNativeBuffer(value.buffer.readRemaining).apply { buffer.writeFully(value.buffer) }
                    next()
                }
                return NativeBytesArray(ldb, keyArray, valueArray, count, handler, options)
            } catch (ex: Throwable) {
                keyArray.filterNotNull().forEach { it.close() }
                valueArray.filterNotNull().forEach { it.close() }
                throw ex
            }
        }

        override fun nextIndirectArray(db: LevelDB, size: Int, bufferSize: Int, options: LevelDB.ReadOptions): LevelDB.Cursor.IndirectValuesArray {
            val keyArray = arrayOfNulls<Allocation>(size)
            val intermediateKeyArray = arrayOfNulls<Allocation>(size)
            val valueArray = arrayOfNulls<Allocation>(size)
            try {
                var count = 0
                for (i in 0 until size) {
                    if (!isValid())
                        break
                    ++count
                    val key = transientKey()
                    val intermediateKey = transientValue()
                    keyArray[i] = Allocation.allocNativeBuffer(key.buffer.readRemaining).apply { buffer.writeFully(key.buffer) }
                    intermediateKeyArray[i] = Allocation.allocNativeBuffer(intermediateKey.buffer.readRemaining).apply { buffer.writeFully(intermediateKey.buffer) }
                    valueArray[i] = (db as LevelDBNative).get(intermediateKey, options)
                    next()
                }
                return NativeIndirectBytesArray(ldb, keyArray, intermediateKeyArray, valueArray, count, handler, this@Cursor.options)
            } catch (ex: Throwable) {
                keyArray.filterNotNull().forEach { it.close() }
                intermediateKeyArray.filterNotNull().forEach { it.close() }
                valueArray.filterNotNull().forEach { it.close() }
                throw ex
            }
        }

        override fun transientKey(): Bytes {
            checkValid()
            return ldbItCall {
                val keySize = alloc<size_tVar>()
                val key = leveldb_iter_key(nonNullPtr, keySize.ptr)!!
                CPointerAllocation(key, keySize.value.convert(), true)
            }
        }

        override fun transientValue(): Bytes {
            checkValid()
            return ldbItCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_iter_value(nonNullPtr, valueSize.ptr)!!
                CPointerAllocation(value, valueSize.value.convert(), true)
            }
        }

        override fun release(ptr: CPointer<leveldb_iterator_t>) {
            leveldb_iter_destroy(ptr)
        }
    }

    override fun newCursor(options: LevelDB.ReadOptions): LevelDB.Cursor {
        options.usePointer { optionsPtr ->
            return Cursor(this, leveldb_create_iterator(nonNullPtr, optionsPtr)!!, dbHandler, this.options)
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    internal class Snapshot(val dbPtr: CPointer<leveldb_t>, ptr: CPointer<leveldb_snapshot_t>, handler: PlatformCloseable.Handler, options: LevelDB.Options) : PointerBound<leveldb_snapshot_t>(ptr, "Snapshot", handler, options), LevelDB.Snapshot {

        override fun release(ptr: CPointer<leveldb_snapshot_t>) {
            leveldb_release_snapshot(dbPtr, ptr)
        }

    }

    override fun newSnapshot(): LevelDB.Snapshot {
        return Snapshot(nonNullPtr, leveldb_create_snapshot(nonNullPtr)!!, dbHandler, options)
    }

    internal class WriteBatch internal constructor(ptr: CPointer<leveldb_writebatch_t>, handler: PlatformCloseable.Handler, options: LevelDB.Options) : PointerBound<leveldb_writebatch_t>(ptr, "WriteBatch", handler, options), LevelDB.WriteBatch {

        override fun put(key: Bytes, value: Bytes) {
            leveldb_writebatch_put(nonNullPtr, key.content, key.buffer.readRemaining.convert(), value.content, value.buffer.readRemaining.convert())
        }

        override fun delete(key: Bytes) {
            leveldb_writebatch_delete(nonNullPtr, key.content, key.buffer.readRemaining.convert())
        }

        override fun release(ptr: CPointer<leveldb_writebatch_t>) {
            leveldb_writebatch_destroy(ptr)
        }

        override fun clear() {
            leveldb_writebatch_clear(nonNullPtr)
        }

        override fun append(source: LevelDB.WriteBatch) {
            leveldb_writebatch_append(nonNullPtr, (source as WriteBatch).nonNullPtr)
        }
    }


    override fun newWriteBatch(): LevelDB.WriteBatch {
        return WriteBatch(leveldb_writebatch_create()!!, dbHandler, options)
    }

    override fun release(ptr: CPointer<leveldb_t>) {
        leveldb_close(ptr)
        releaseOptionsPtr(optionsPtrs)
    }

    override fun beforeClose() {
        dbHandler.close()
    }
}
