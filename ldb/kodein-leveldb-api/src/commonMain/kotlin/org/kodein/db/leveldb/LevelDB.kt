package org.kodein.db.leveldb


import kotlinx.io.core.Closeable
import org.kodein.log.LoggerFactory

/**
 * Google's LevelDB in Java.
 *
 * This interface allows the use of different implementations of LevelDB.
 */
interface LevelDB : Closeable, PlatformLevelDB {

    /**
     * path of the DB.
     */
    val path: String

    /**
     * Put an entry into the database.
     *
     * @param key The key of the entry to put.
     * @param value The value ov the entry to put.
     * @param options Options that control this write operation.
     */
    fun put(key: Bytes, value: Bytes, options: WriteOptions = WriteOptions.DEFAULT)

    /**
     * Delete an entry from the database.
     *
     * @param key The key of the entry to delete.
     * @param options Options that control this write operation.
     */
    fun delete(key: Bytes, options: WriteOptions = WriteOptions.DEFAULT)

    /**
     * Write a batch atomically to the database.
     *
     * All modifications that were registerd in that batch will be reflected into the database at once.
     * It is therefore impossible to create a Snapshot of the database that contains only parts of the modifications.
     *
     * @see .NewWriteBatch
     * @see WriteBatch
     *
     * @param batch The batch to write.
     * @param options Options that control this write operation.
     */
    fun write(batch: WriteBatch, options: WriteOptions = WriteOptions.DEFAULT)

    /**
     * Get an entry value bytes from the database from its key.
     *
     * **Warning**: The returned bytes are `Closeable`!
     *
     * @param key The key of the entry to get.
     * @param options Options that control this read operation.
     * @return The entry value.
     */
    fun get(key: Bytes, options: ReadOptions = ReadOptions.DEFAULT): Allocation?

    /**
     * Get an entry value bytes by following the value of the given key.
     *
     * This function will get the value associated with this key, use this value as second key and return the value associated with this second key.
     *
     * This is equivalent (but a lot faster) to:
     *
     * ```kotlin
     * db.Get(key, options).use { indirection ->
     *     return db.Get(indirection.getBuffer(), options)
     * }
     * ```
     *
     * @param key The key of the entry to follow.
     * @param options Options that control this read operation.
     * @return The found entry value
     */
    fun indirectGet(key: Bytes, options: ReadOptions = ReadOptions.DEFAULT): Allocation?

    /**
     * Get an entry value bytes by following the value of the cursor current key.
     *
     * This function will get the value associated with this key, use this value as second key and return the value associated with this second key.
     *
     * This is equivalent (but a lot faster) to:
     *
     * ```java
     * db.Get(cursor.transientValue(), options);
     * ```
     *
     * @param cursor The cursor that's position on the entry to follow.
     * @param options Options that control this read operation.
     * @return The found entry value
     */
    fun indirectGet(cursor: Cursor, options: ReadOptions = ReadOptions.DEFAULT): Allocation?

    /**
     * Creates a new Cursor.
     *
     * **Warning**: Cursors are `Closeable`!
     *
     * Note that, if snapshot is null, you can iterate on entries that were inserted after the creation of the Cursor.
     *
     * @see Cursor
     *
     * @param options Options that control this Cursors read operations.
     * @return A new Cursor.
     */
    fun newCursor(options: ReadOptions = ReadOptions.DEFAULT): Cursor

    /**
     * Creates a new Snapshot.
     *
     * **Warning**: Snapshots are `Closeable`!
     *
     * @see Snapshot
     *
     * @see .Write
     * @return A new Snapshot.
     */
    fun newSnapshot(): Snapshot

    /**
     * Creates a new WriteBatch.
     *
     * **Warning**: WriteBatches are `Closeable`!
     *
     * @see WriteBatch
     *
     * @see .Write
     * @return A new WriteBatch.
     */
    fun newWriteBatch(): WriteBatch

    /**
     * A WriteBatch is a write only object.
     * When writing on the batch, the database is **not** reflected.
     *
     * All the modifications that are done using this batch will be actually reflected onto the database when calling Write with it.
     *
     * @see .NewWriteBatch
     * @see .Write
     */
    interface WriteBatch : Closeable, PlatformLevelDB.WriteBatch {

        /**
         * Registers an entry to be put into the database.
         *
         * @param key The key of the entry to put.
         * @param value The value ov the entry to put.
         */
        fun put(key: Bytes, value: Bytes)

        /**
         * Registers a key whose entry is to be deleted from the database.
         *
         * @param key The key of the entry to delete.
         */
        fun delete(key: Bytes)
    }

    /**
     * A snapshot is a read-only view of the database at the moment it is created.
     * Later modifications to the database will not be reflected to the snapshot.
     *
     * @see .NewSnapshot
     * @see .Write
     */
    interface Snapshot : Closeable

    /**
     * An Cursor to iterate over the entries of a database or a Snapshot.
     */
    interface Cursor : Closeable, PlatformLevelDB.Cursor {

        /**
         * @return Whether or not the cursor is in a valid state.
         */
        fun isValid(): Boolean

        /**
         * Position the cursor on the first entry of the database.
         */
        fun seekToFirst()

        /**
         * Position the cursor on the last entry of the database.
         */
        fun seekToLast()

        /**
         * Position the cursor to the entry corresponding to the provided key inside the database.
         *
         * If the key does not exists, it will be positioned to the next valid entry whose key would be placed after the provided key.
         *
         * @param target The key to seek to.
         */
        fun seekTo(target: Bytes)

        /**
         * Position the cursor on the entry right next after the current one.
         *
         * Note that if this cursor was created without a Snapshot, it can be positionned on an entry that were inserted after the creation of the cursor.
         */
        fun next()

        /**
         * An array of keys and values.
         */
        interface ValuesArrayBase : Closeable {

            /**
             * @return The number of entries in this array.
             */
            val size: Int

            /**
             * @param i An index.
             * @return The key at index i.
             */
            fun getKey(i: Int): Bytes

            /**
             * @param i An index.
             * @return The value at index i.
             */
            fun getValue(i: Int): Bytes?
        }

        /**
         * An array of keys and values.
         */
        interface ValuesArray : ValuesArrayBase {

            /**
             * @param i An index.
             * @return The value at index i.
             */
            override fun getValue(i: Int): Bytes
        }

        /**
         * An array of keys, intermediate keys and values.
         */
        interface IndirectValuesArray : ValuesArrayBase {

            /**
             * @param i An index.
             * @return The key at index i.
             */
            fun getIntermediateKey(i: Int): Bytes
        }

        /**
         * Get an array of the next entries and position the cursor to the entry after the last one in the returned array.
         *
         * The point of doing this is optimisation: it enables only one JNI access to fecth a large set of entries, thus limiting JNI access and allowing meaningful JIT optimisation by the JVM.
         *
         * This function will create as little byte buffers as possible.
         * Each byte buffer will have a memory range allocated with the size of bufferSize.
         * If bufferSize is big enough, this means that there should be a lot less byte buffers than there are results as each byte buffers should contain many results, and therefore a lot less GC.
         * However, the bigger the bufferSize, the less GC, but the smallest bufferSize, the less unused memory and therefore a better memory footprint.
         * Note that a bigger memory allocation then bufferSize can happen if it is needed to contain a single entry that's biggest than bufferSize.
         *
         * @param size The maximum number of entries to get.
         * @param bufferSize The size allocated for each direct byte buffer.
         */
        fun nextArray(size: Int, bufferSize: Int = -1): ValuesArray

        fun nextIndirectArray(db: LevelDB, size: Int, bufferSize: Int = -1, options: ReadOptions = ReadOptions.DEFAULT): Cursor.IndirectValuesArray

        /**
         * Position the cursor on the entry right before the current one.
         *
         * Note that if this cursor was created without a Snapshot, it can be positionned on an entry that were inserted after the creation of the cursor.
         */
        fun prev()

        /**
         * Get a Buffer containing the current key.
         *
         * Note that this Buffer is valid **as long as the cursor is not moved**.
         *
         * @return The key bytes.
         */
        fun transientKey(): Bytes

        /**
         * Get a Buffer containing the current value.
         *
         * Note that this Buffer is valid **as long as the cursor is not moved**.
         *
         * @return The value bytes.
         */
        fun transientValue(): Bytes

        override fun close()
    }

    /**
     * Defines how to react if the database exists or not.
     */
    enum class OpenPolicy(val createIfMissing: Boolean, val errorIfExists: Boolean) {
        /**
         * Open the database if it exists, fail otherwise.
         */
        OPEN(createIfMissing = false, errorIfExists = false),

        /**
         * Create a new database if it does not exist, then open it.
         */
        OPEN_OR_CREATE(createIfMissing = true, errorIfExists = false),

        /**
         * Create a new database if it does not exist, fail otherwise.
         */
        CREATE(createIfMissing = true, errorIfExists = true)
    }

    /**
     * Options to control the behavior of a database.
     */
    data class Options
    (
            /**
             * Defines how to react if the database exists or not.
             *
             * (Default: OpenPolicy.OPEN_OR_CREATE)
             *
             * @see OpenPolicy
             */
            val openPolicy: OpenPolicy = OpenPolicy.OPEN_OR_CREATE,

            /**
             * If true, the implementation will do aggressive checking of the data it is processing and will stop early if it detects any errors.
             *
             * This may have unforeseen ramifications: for example, a corruption of one DB entry may cause a large number of entries to become unreadable or for the entire DB to become unopenable.
             *
             * (Default: false)
             */
            val paranoidChecks: Boolean = false,

            /**
             * If true, the LevelDB implementation will print internal logs.
             *
             * This severly slows the database and should never be set in production.
             *
             * (Default: false)
             */
            val printLogs: Boolean = false,

            /**
             * Amount of data to build up in memory (backed by an unsorted log on disk) before converting to a sorted on-disk file.
             *
             * Larger values increase performance, especially during bulk loads.
             * Up to two write buffers may be held in memory at the same time, so you may wish to adjust this parameter to control memory usage.
             * Also, a larger write buffer will result in a longer recovery time the next time the database is opened.
             *
             * (Default: 4MB)
             */
            val writeBufferSize: Int = 4 shl 20,

            /**
             * Number of open files that can be used by the DB.
             *
             * You may need to increase this if your database has a large working set (budget one open file per 2MB of working set).
             *
             * (Default: 1000)
             */
            val maxOpenFiles: Int = 1000,

            /**
             * Size of the LRU cache LevelDB will use to prevent unneeded disk access.
             *
             * (Default: 8MB)
             */
            val cacheSize: Int = 8 shl 20,

            /**
             * Approximate size of user data packed per block.
             *
             * Note that the block size specified here corresponds to uncompressed data.
             * The actual size of the unit read from disk may be smaller if compression is enabled.
             *
             * This parameter can be changed dynamically.
             *
             * (Default: 4K)
             */
            val blockSize: Int = 4096,

            /**
             * Number of keys between restart points for delta encoding of keys.
             *
             * Most clients should leave this parameter alone.
             *
             * This parameter can be changed dynamically.
             *
             * (Default: 16)
             */
            val blockRestartInterval: Int = 16,

            /**
             * Leveldb will write up to this amount of bytes to a file before switching to a new one.
             *
             * Most clients should leave this parameter alone.
             * However if your filesystem is more efficient with larger files, you could consider increasing the value.
             * The downside will be longer compactions and hence longer latency/performance hiccups.
             * Another reason to increase this parameter might be when you are initially populating a large database.
             *
             * (Default: 2MB)
             */
            val maxFileSize: Int = 2 shl 20,

            /**
             * Whether to compress blocks using the Snappy compression algorithm.
             *
             * Snappy compression gives lightweight but fast compression.
             *
             * Typical speeds of Snappy compression on an Intel(R) Core(TM)2 2.4GHz:
             *
             * - ~200-500MB/s compression
             * - ~400-800MB/s decompression
             *
             * Note that these speeds are significantly faster than most persistent storage speeds, and therefore it is typically never worth switching off.
             * Even if the input data is incompressible, the Snappy compression implementation will efficiently detect that and will switch to uncompressed mode.
             *
             * This parameter can be changed dynamically.
             *
             * (Default: true)
             */
            val snappyCompression: Boolean = true,

            /**
             * If non-0, use a Bloom filter policy to reduce disk reads.
             *
             * Uses a bloom filter with approximately the specified number of bits per key.
             * A good value is 10, which yields a filter with ~ 1% false positive rate.
             *
             * (Default: 10)
             */
            val bloomFilterBitsPerKey: Int = 10,

            /**
             * If a DB cannot be opened, you may attempt to set this to true to resurrect as much of the contents of the database as possible.
             *
             * Some data may be lost, so be careful when setting this on a database that contains important information.
             */
            val repairOnCorruption: Boolean = false,

            val loggerFactory: LoggerFactory? = null,

            val trackClosableAllocation: Boolean = false,

            val defaultCursorArrayBufferSize: Int = 4096
    ) {
        companion object {
            val DEFAULT = Options()
        }
    }

    /**
     * Options that control read operations.
     */
    data class ReadOptions(
            /**
             * If true, all data read from underlying storage will be verified against corresponding checksums.
             *
             * (Default: false)
             */
            val verifyChecksums: Boolean = false,

            /**
             * Should the data read for this iteration be cached in memory?
             *
             * Callers may wish to set this field to false for bulk scans.
             *
             * (Default: true)
             */
            val fillCache: Boolean = true,

            /**
             * If "snapshot" is non-NULL, read as of the supplied snapshot (which must belong to the DB that is being read and which must not have been released).
             */
            val snapshot: Snapshot? = null
    ) {
        companion object {
            val DEFAULT = ReadOptions()
        }
    }

    /**
     * Options that control write operations.
     */
    data class WriteOptions(
            /**
             * If true, the write will be flushed from the operating system buffer cache (by calling WritableFile::Sync()) before the write is considered complete.
             *
             * If this flag is true, writes will be slower.
             *
             * If this flag is false, and the machine crashes, some recent writes may be lost.
             * Note that if it is just the process that crashes (i.e., the machine does not reboot), no writes will be lost even if sync==false.
             *
             * In other words, a DB write with sync==false has similar crash semantics as the "write()" system call.
             * A DB write with sync==true has similar crash semantics to a "write()" system call followed by "fsync()".
             *
             * (Default: false)
             */
            val sync: Boolean = false
    ) {
        companion object {
            val DEFAULT = WriteOptions()
        }
    }

    /**
     * A Sober-LevelDB factory is responsible for opening or destroying LevelDB databases.
     */
    interface Factory {
        /**
         * Open or create a LevelDB database.
         *
         * The cache will be LevelDB default 8MB cache.
         *
         * @param path The path to the database to open.
         * @param options The database options.
         * @return The LevelDB Java object.
         */
        fun open(path: String, options: Options = Options.DEFAULT): LevelDB

        /**
         * Destroy a LevelDB database, if it exists.
         *
         * @param path The path to the database to destroy.
         */
        fun destroy(path: String, options: Options = Options.DEFAULT)

        class Based(val baseWithSeparator: String, val factory: Factory) : Factory {
            override fun open(path: String, options: Options) = factory.open(baseWithSeparator + path, options)
            override fun destroy(path: String, options: Options) = factory.destroy(baseWithSeparator + path, options)
        }
    }
}

