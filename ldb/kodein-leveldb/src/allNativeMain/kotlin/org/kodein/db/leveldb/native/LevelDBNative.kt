@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.kodein.db.leveldb.native

import cnames.structs.leveldb_cache_t
import cnames.structs.leveldb_filterpolicy_t
import cnames.structs.leveldb_iterator_t
import cnames.structs.leveldb_options_t
import cnames.structs.leveldb_readoptions_t
import cnames.structs.leveldb_snapshot_t
import cnames.structs.leveldb_t
import cnames.structs.leveldb_writebatch_t
import cnames.structs.leveldb_writeoptions_t
import kotlinx.cinterop.*
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBException
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.libleveldb.*
import org.kodein.memory.io.*
import platform.posix.size_tVar

private inline fun <T> ldbCall(crossinline block: MemScope.(CPointerVar<ByteVar>) -> T): T = memScoped {
    val errorPtr = allocPointerTo<ByteVar>()
    val ret = block(errorPtr)
    errorPtr.value?.let { error ->
        val errorStr = error.toKString()
        throw LevelDBException(errorStr)
    }
    ret
}

public class OptionsPtrs(public val options: CPointer<leveldb_options_t>, public val cache: CPointer<leveldb_cache_t>?, public val filterPolicy: CPointer<leveldb_filterpolicy_t>?)

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


public class LevelDBNative private constructor(ptr: CPointer<leveldb_t>, options: LevelDB.Options, private val optionsPtrs: OptionsPtrs, override val path: String) : PointerBound<leveldb_t>(ptr, "DB", null, options), LevelDB {

    private val dbHandler = Handler()

    public companion object Factory : LevelDBFactory {

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

    override fun put(key: ReadMemory, value: ReadMemory, options: LevelDB.WriteOptions) {
        options.usePointer { optionsPtr ->
            ldbCall { leveldb_put(nonNullPtr, optionsPtr, key.pointer(), key.size.convert(), value.pointer(), value.size.convert(), it.ptr) }
        }
    }

    override fun delete(key: ReadMemory, options: LevelDB.WriteOptions) {
        options.usePointer { optionsPtr ->
            ldbCall { leveldb_delete(nonNullPtr, optionsPtr, key.pointer(), key.size.convert(), it.ptr) }
        }
    }

    override fun write(batch: LevelDB.WriteBatch, options: LevelDB.WriteOptions) {
        options.usePointer { optionsPtr ->
            ldbCall { leveldb_write(nonNullPtr, optionsPtr, (batch as WriteBatch).nonNullPtr, it.ptr) }
        }
    }

    internal class NativeBytes(ptr: CPointer<ByteVar>, len: Int, handler: Handler, options: LevelDB.Options, private val buffer: KBuffer = KBuffer.wrap(ptr, len))
        : PointerBound<ByteVar>(ptr, "Value", handler, options), Allocation, KBuffer by buffer {

        override fun release(ptr: CPointer<ByteVar>) {
            nativeHeap.free(ptr)
        }
    }

    override fun get(key: ReadMemory, options: LevelDB.ReadOptions): Allocation? {
        options.usePointer { optionsPtr ->
            return ldbCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_get(nonNullPtr, optionsPtr, key.pointer(), key.size.convert(), valueSize.ptr, it.ptr)
                if (value != null)
                    NativeBytes(value, valueSize.value.toInt(), dbHandler, this@LevelDBNative.options)
                else
                    null
            }
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    override fun indirectGet(key: ReadMemory, options: LevelDB.ReadOptions): Allocation? {
        options.usePointer { optionsPtr ->
            val (newKey, newKeySize) = ldbCall {
                val newKeySize = alloc<size_tVar>()
                val newKey = leveldb_get(nonNullPtr, optionsPtr, key.pointer(), key.size.convert(), newKeySize.ptr, it.ptr)
                newKey to newKeySize.value
            }
            if (newKey == null)
                return null
            return ldbCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_get(nonNullPtr, optionsPtr, newKey, newKeySize, valueSize.ptr, it.ptr)
                if (value != null)
                    NativeBytes(value, valueSize.value.toInt(), dbHandler, this@LevelDBNative.options)
                else
                    null
            }
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    override fun indirectGet(cursor: LevelDB.Cursor, options: LevelDB.ReadOptions): Allocation? {
        (cursor as Cursor).checkValid()

        val newKey = cursor.transientValue()

        return get(newKey, options)
    }

    internal class Cursor internal constructor(ptr: CPointer<leveldb_iterator_t>, handler: Handler, options: LevelDB.Options) : PointerBound<leveldb_iterator_t>(ptr, "Cursor", handler, options), LevelDB.Cursor {

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

        override fun seekTo(target: ReadMemory) {
            ldbItCall { leveldb_iter_seek(nonNullPtr, target.pointer(), target.size.convert()) }
        }

        override fun next() {
            checkValid()
            ldbItCall { leveldb_iter_next(nonNullPtr) }
        }

        override fun prev() {
            checkValid()
            ldbItCall { leveldb_iter_prev(nonNullPtr) }
        }

        override fun transientKey(): KBuffer {
            checkValid()
            return ldbItCall {
                val keySize = alloc<size_tVar>()
                val key = leveldb_iter_key(nonNullPtr, keySize.ptr)!!
                KBuffer.wrap(key, keySize.value.toInt())
            }
        }

        override fun transientValue(): KBuffer {
            checkValid()
            return ldbItCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_iter_value(nonNullPtr, valueSize.ptr)!!
                KBuffer.wrap(value, valueSize.value.toInt())
            }
        }

        override fun release(ptr: CPointer<leveldb_iterator_t>) {
            leveldb_iter_destroy(ptr)
        }
    }

    override fun newCursor(options: LevelDB.ReadOptions): LevelDB.Cursor {
        options.usePointer { optionsPtr ->
            return Cursor(leveldb_create_iterator(nonNullPtr, optionsPtr)!!, dbHandler, this.options)
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    internal class Snapshot(private val dbPtr: CPointer<leveldb_t>, ptr: CPointer<leveldb_snapshot_t>, handler: Handler, options: LevelDB.Options) : PointerBound<leveldb_snapshot_t>(ptr, "Snapshot", handler, options), LevelDB.Snapshot {

        override fun release(ptr: CPointer<leveldb_snapshot_t>) {
            leveldb_release_snapshot(dbPtr, ptr)
        }

    }

    override fun newSnapshot(): LevelDB.Snapshot {
        return Snapshot(nonNullPtr, leveldb_create_snapshot(nonNullPtr)!!, dbHandler, options)
    }

    internal class WriteBatch internal constructor(ptr: CPointer<leveldb_writebatch_t>, handler: Handler, options: LevelDB.Options) : PointerBound<leveldb_writebatch_t>(ptr, "WriteBatch", handler, options), LevelDB.WriteBatch {

        override fun put(key: ReadMemory, value: ReadMemory) {
            leveldb_writebatch_put(nonNullPtr, key.pointer(), key.size.convert(), value.pointer(), value.size.convert())
        }

        override fun delete(key: ReadMemory) {
            leveldb_writebatch_delete(nonNullPtr, key.pointer(), key.size.convert())
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
