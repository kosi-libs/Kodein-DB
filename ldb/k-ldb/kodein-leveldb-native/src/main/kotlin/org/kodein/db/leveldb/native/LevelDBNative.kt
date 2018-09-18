package org.kodein.db.leveldb.native

import cnames.structs.leveldb_iterator_t
import cnames.structs.leveldb_options_t
import cnames.structs.leveldb_readoptions_t
import cnames.structs.leveldb_snapshot_t
import cnames.structs.leveldb_t
import cnames.structs.leveldb_writebatch_t
import cnames.structs.leveldb_writeoptions_t
import kotlinx.cinterop.*
import kotlinx.io.core.writeFully
import libleveldb.*
import org.kodein.db.leveldb.*
import platform.posix.size_tVar

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

private fun LevelDB.Options.allocOptionsPtr(): CPointer<leveldb_options_t> {
    val optionsPtr = leveldb_options_create()!!
    leveldb_options_set_info_log(optionsPtr, null)
    leveldb_options_set_create_if_missing(optionsPtr, openPolicy.createIfMissing.toByte().toUByte())
    leveldb_options_set_error_if_exists(optionsPtr, openPolicy.errorIfExists.toByte().toUByte())
    leveldb_options_set_paranoid_checks(optionsPtr, paranoidChecks.toByte().toUByte())
    leveldb_options_set_write_buffer_size(optionsPtr, writeBufferSize.convert())
    leveldb_options_set_max_open_files(optionsPtr, maxOpenFiles)
    leveldb_options_set_cache(optionsPtr, leveldb_cache_create_lru(cacheSize.convert()))
    leveldb_options_set_block_size(optionsPtr, blockSize.convert())
    leveldb_options_set_block_restart_interval(optionsPtr, blockRestartInterval)
    leveldb_options_set_compression(optionsPtr, if (snappyCompression) leveldb_snappy_compression.toInt() else leveldb_no_compression.toInt())
    leveldb_options_set_filter_policy(optionsPtr, if (bloomFilterBitsPerKey == 0) null else leveldb_filterpolicy_create_bloom(bloomFilterBitsPerKey))
    return optionsPtr
}

//@kotlin.contracts.ExperimentalContracts
private inline fun LevelDB.Options.usePointer(block: (CPointer<leveldb_options_t>) -> Unit) {
//    kotlin.contracts.contract {
//        callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
//    }
    val ptr = allocOptionsPtr()
    try {
        block(ptr)
    }
    finally {
        leveldb_options_destroy(ptr)
    }
}

private fun LevelDB.ReadOptions.allocOptionsPtr(): CPointer<leveldb_readoptions_t> {
    val optionsPtr = leveldb_readoptions_create()!!
    leveldb_readoptions_set_fill_cache(optionsPtr, fillCache.toByte().toUByte())
    leveldb_readoptions_set_verify_checksums(optionsPtr, verifyChecksums.toByte().toUByte())
    leveldb_readoptions_set_snapshot(optionsPtr, (snapshot as LevelDBNative.Snapshot?)?.nonNullPtr)
    return optionsPtr
}

private inline fun LevelDB.ReadOptions.usePointer(block: (CPointer<leveldb_readoptions_t>) -> Unit) {
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


class LevelDBNative private constructor(ptr: CPointer<leveldb_t>, options: LevelDB.Options) : PointerBound<leveldb_t>(ptr, "DB", null, options), LevelDB {

    private val dbHandler = PlatformCloseable.Handler()

    object Factory : LevelDB.Factory {

        override fun open(path: String, options: LevelDB.Options): LevelDB {
            options.usePointer { optionsPtr ->
                val dbPtr = ldbCall { leveldb_open(optionsPtr, path, it.ptr) } ?: throw LevelDBException("Unknown error")
                return LevelDBNative(dbPtr, options)
            }
            throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
        }

        override fun destroy(path: String, options: LevelDB.Options) {
            options.usePointer { optionsPtr ->
                ldbCall { leveldb_destroy_db(optionsPtr, path, it.ptr) }
            }
        }
    }

    override fun put(key: Allocation, value: Allocation, options: LevelDB.WriteOptions) {
        options.usePointer { optionsPtr ->
            ldbCall { leveldb_put(nonNullPtr, optionsPtr, key.content, key.io.readRemaining.convert(), value.content, value.io.readRemaining.convert(), it.ptr) }
        }
    }

    override fun delete(key: Allocation, options: LevelDB.WriteOptions) {
        options.usePointer { optionsPtr ->
            ldbCall { leveldb_delete(nonNullPtr, optionsPtr, key.content, key.io.readRemaining.convert(), it.ptr) }
        }
    }

    override fun write(batch: LevelDB.WriteBatch, options: LevelDB.WriteOptions) {
        options.usePointer { optionsPtr ->
            ldbCall { leveldb_write(nonNullPtr, optionsPtr, (batch as WriteBatch).nonNullPtr, it.ptr) }
        }
    }

    internal class NativeBytes(ptr: CPointer<ByteVar>, len: Int, handler: PlatformCloseable.Handler, options: LevelDB.Options) : PointerBound<ByteVar>(ptr, "Value", handler, options), LevelDB.NativeBytes {

        override val allocation: Allocation = Allocation(ptr, len, true)
            get() {
                checkIsOpen()
                return field
            }

        override fun release(ptr: CPointer<ByteVar>) {
            nativeHeap.free(ptr)
        }
    }

    override fun get(key: Allocation, options: LevelDB.ReadOptions): LevelDB.NativeBytes? {
        options.usePointer { optionsPtr ->
            return ldbCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_get(nonNullPtr, optionsPtr, key.content, key.io.readRemaining.convert(), valueSize.ptr, it.ptr)
                if (value != null)
                    NativeBytes(value, valueSize.value.convert(), dbHandler, this@LevelDBNative.options)
                else
                    null

            }
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    override fun indirectGet(key: Allocation, options: LevelDB.ReadOptions): LevelDB.NativeBytes? {
        options.usePointer { optionsPtr ->
            val (newKey, newKeySize) = ldbCall {
                val newKeySize = alloc<size_tVar>()
                val newKey = leveldb_get(nonNullPtr, optionsPtr, key.content, key.io.readRemaining.convert(), newKeySize.ptr, it.ptr)
                newKey to newKeySize
            }
            if (newKey == null)
                return null
            return ldbCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_get(nonNullPtr, optionsPtr, newKey, newKeySize.value, valueSize.ptr, it.ptr)
                if (value != null)
                    NativeBytes(value, valueSize.value.convert(), dbHandler, this@LevelDBNative.options)
                else
                    null
            }
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    override fun indirectGet(it: LevelDB.Iterator, options: LevelDB.ReadOptions): LevelDB.NativeBytes? {
        (it as Iterator).checkValid()

        val newKey = it.transientValue()

        options.usePointer { optionsPtr ->
            return ldbCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_get(nonNullPtr, optionsPtr, newKey.content, newKey.io.readRemaining.convert(), valueSize.ptr, it.ptr)
                if (value != null)
                    NativeBytes(value, valueSize.value.convert(), dbHandler, this@LevelDBNative.options)
                else
                    null
            }
        }
        throw IllegalStateException() // TODO: Wait for contracts to become outside of experimental
    }

    internal class Iterator internal constructor(val ldb: LevelDB, ptr: CPointer<leveldb_iterator_t>, handler: PlatformCloseable.Handler, options: LevelDB.Options) : PointerBound<leveldb_iterator_t>(ptr, "Iterator", handler, options), LevelDB.Iterator {

        internal fun checkValid() {
            if (!isValid())
                throw LevelDBException("Iterator is not valid")
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

        override fun seekTo(target: Allocation) {
            ldbItCall { leveldb_iter_seek(nonNullPtr, target.content, target.io.readRemaining.convert()) }
        }

        override fun next() {
            checkValid()
            ldbItCall { leveldb_iter_next(nonNullPtr) }
        }

        override fun prev() {
            checkValid()
            ldbItCall { leveldb_iter_prev(nonNullPtr) }
        }

        internal class NativeBytesArray(
                val ldb: LevelDB,
                val keys: Array<Allocation?>,
                val values: Array<Allocation?>,
                override val size: Int,
                handler: PlatformCloseable.Handler?,
                options: LevelDB.Options
        ) : PlatformCloseable("IteratorArray", handler, options), LevelDB.Iterator.NativeBytesArray {

            override var isClosed = false

            override fun platformClose() {
                for (i in 0 until size) {
                    ldb.releaseBuffer(keys[i]!!)
                    ldb.releaseBuffer(values[i]!!)
                }
                isClosed = true
            }

            private fun checkIndex(i: Int) {
                checkIsOpen()
                if (i > size)
                    throw ArrayIndexOutOfBoundsException("Index $i is over array size: $size")
            }

            override fun getKey(i: Int): Allocation {
                checkIndex(i)
                return keys[i]!!
            }

            override fun getValue(i: Int): Allocation {
                checkIndex(i)
                return values[i]!!
            }

        }

        override fun nextArray(size: Int, bufferSize: Int): LevelDB.Iterator.NativeBytesArray {
            val keyArray = arrayOfNulls<Allocation>(size)
            val valueArray = arrayOfNulls<Allocation>(size)
            var count = 0
            for (i in 0 until size) {
                if (!isValid())
                    break
                ++count
                val key = transientKey()
                val value = transientValue()
                val keyCopy = ldb.allocBuffer(key.io.readRemaining).apply { io.writeFully(key.io) }
                val valueCopy = ldb.allocBuffer(value.io.readRemaining).apply { io.writeFully(value.io) }
                keyArray[i] = keyCopy
                valueArray[i] = valueCopy
                next()
            }
            return NativeBytesArray(ldb, keyArray, valueArray, count, handler, options)
        }

        override fun transientKey(): Allocation {
            checkValid()
            return ldbItCall {
                val keySize = alloc<size_tVar>()
                val key = leveldb_iter_key(nonNullPtr, keySize.ptr)!!
                Allocation(key, keySize.value.convert(), true)
            }
        }

        override fun transientValue(): Allocation {
            checkValid()
            return ldbItCall {
                val valueSize = alloc<size_tVar>()
                val value = leveldb_iter_value(nonNullPtr, valueSize.ptr)!!
                Allocation(value, valueSize.value.convert(), true)
            }
        }

        override fun release(ptr: CPointer<leveldb_iterator_t>) {
            leveldb_iter_destroy(ptr)
        }
    }

    override fun newIterator(options: LevelDB.ReadOptions): LevelDB.Iterator {
        options.usePointer { optionsPtr ->
            return Iterator(this, leveldb_create_iterator(nonNullPtr, optionsPtr)!!, dbHandler, this.options)
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

        override fun put(key: Allocation, value: Allocation) {
            leveldb_writebatch_put(nonNullPtr, key.content, key.io.readRemaining.convert(), value.content, value.io.readRemaining.convert())
        }

        override fun delete(key: Allocation) {
            leveldb_writebatch_delete(nonNullPtr, key.content, key.io.readRemaining.convert())
        }

        override fun release(ptr: CPointer<leveldb_writebatch_t>) {
            leveldb_writebatch_destroy(ptr)
        }
    }


    override fun newWriteBatch(): LevelDB.WriteBatch {
        return WriteBatch(leveldb_writebatch_create()!!, dbHandler, options)
    }

    override fun release(ptr: CPointer<leveldb_t>) {
        leveldb_close(ptr)
    }

    override fun beforeClose() {
        dbHandler.close()
    }
}
