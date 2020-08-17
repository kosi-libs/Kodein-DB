package org.kodein.db.leveldb


import org.kodein.log.LoggerFactory
import org.kodein.memory.Closeable
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.ReadMemory

/**
 * Google's LevelDB in Java.
 *
 * This interface allows the use of different implementations of LevelDB.
 */
public interface LevelDB : Closeable {

    /**
     * path of the DB.
     */
    public val path: String

    /**
     * Put an entry into the database.
     *
     * @param key The key of the entry to put.
     * @param value The value ov the entry to put.
     * @param options Options that control this write operation.
     */
    public fun put(key: ReadMemory, value: ReadMemory, options: WriteOptions = WriteOptions.DEFAULT)

    /**
     * Delete an entry from the database.
     *
     * @param key The key of the entry to delete.
     * @param options Options that control this write operation.
     */
    public fun delete(key: ReadMemory, options: WriteOptions = WriteOptions.DEFAULT)

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
    public fun write(batch: WriteBatch, options: WriteOptions = WriteOptions.DEFAULT)

    /**
     * Get an entry value bytes from the database from its key.
     *
     * **Warning**: The returned bytes are `Closeable`!
     *
     * @param key The key of the entry to get.
     * @param options Options that control this read operation.
     * @return The entry value.
     */
    public fun get(key: ReadMemory, options: ReadOptions = ReadOptions.DEFAULT): Allocation?

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
    public fun indirectGet(key: ReadMemory, options: ReadOptions = ReadOptions.DEFAULT): Allocation?

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
    public fun indirectGet(cursor: Cursor, options: ReadOptions = ReadOptions.DEFAULT): Allocation?

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
    public fun newCursor(options: ReadOptions = ReadOptions.DEFAULT): Cursor

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
    public fun newSnapshot(): Snapshot

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
    public fun newWriteBatch(): WriteBatch

    public companion object {}

    // TODO: Add these methods
//    /**
//     * DB implementations can export properties about their state via this method.
//     *
//     * @return The value of the property, if found.
//    */
//    fun getProperty(): Allocation?
//
//    /**
//     * Compact the underlying storage for the key range [begin,end].
//     *
//     * The compactRange(null, null) will compact the entire database.
//     *
//     * @param begin The beginning of the range, or null to start at the first key.
//     * @param limit The inclusive limit of the range, or null to end at the last key.
//     */
//    fun compactRange(begin: Bytes?, limit: Bytes?)
//
//    /**
//     * The approximate file system space used by keys in each range (inclusive)
//     *
//     * The results may not include the sizes of recently written data.
//     *
//     * @param ranges A list of inclusive ranges to get their sizes.
//     * @return The corresponding list of sizes.
//     */
//    fun getApproximateSizes(ranges: List<Pair<Byte, Byte>>): Array<Int>

    /**
     * A WriteBatch is a write only object.
     * When writing on the batch, the database is **not** reflected.
     *
     * All the modifications that are done using this batch will be actually reflected onto the database when calling Write with it.
     *
     * @see .NewWriteBatch
     * @see .Write
     */
    public interface WriteBatch : Closeable {

        /**
         * Registers an entry to be put into the database.
         *
         * @param key The key of the entry to put.
         * @param value The value ov the entry to put.
         */
        public fun put(key: ReadMemory, value: ReadMemory)

        /**
         * Registers a key whose entry is to be deleted from the database.
         *
         * @param key The key of the entry to delete.
         */
        public fun delete(key: ReadMemory)

        /**
         * Clear all updates buffered in this batch.
         */
        public fun clear()

        public fun append(source: WriteBatch)

        // TODO: Add these methods
//        interface Handler {
//            fun put(key: Bytes, value: Bytes)
//            fun delete(key: Bytes, value: Bytes)
//        }
//
//        fun iterate(handler: Handler)
//
//        fun getApproximateSize()

    }

    /**
     * A snapshot is a read-only view of the database at the moment it is created.
     * Later modifications to the database will not be reflected to the snapshot.
     *
     * @see .NewSnapshot
     * @see .Write
     */
    public interface Snapshot : Closeable

    /**
     * An Cursor to iterate over the entries of a database or a Snapshot.
     */
    public interface Cursor : Closeable {

        /**
         * @return Whether or not the cursor is in a valid state.
         */
        public fun isValid(): Boolean

        /**
         * Position the cursor on the first entry of the database.
         */
        public fun seekToFirst()

        /**
         * Position the cursor on the last entry of the database.
         */
        public fun seekToLast()

        /**
         * Position the cursor to the entry corresponding to the provided key inside the database.
         *
         * If the key does not exists, it will be positioned to the next valid entry whose key would be placed after the provided key.
         *
         * @param target The key to seek to.
         */
        public fun seekTo(target: ReadMemory)

        /**
         * Position the cursor on the entry right next after the current one.
         *
         * Note that if this cursor was created without a Snapshot, it can be positionned on an entry that were inserted after the creation of the cursor.
         */
        public fun next()

        /**
         * Position the cursor on the entry right before the current one.
         *
         * Note that if this cursor was created without a Snapshot, it can be positionned on an entry that were inserted after the creation of the cursor.
         */
        public fun prev()

        /**
         * Get a Buffer containing the current key.
         *
         * Note that this Buffer is valid **as long as the cursor is not moved**.
         *
         * @return The key bytes.
         */
        public fun transientKey(): ReadBuffer

        /**
         * Get a Buffer containing the current value.
         *
         * Note that this Buffer is valid **as long as the cursor is not moved**.
         *
         * @return The value bytes.
         */
        public fun transientValue(): ReadBuffer

        override fun close()
    }

    /**
     * Defines how to react if the database exists or not.
     */
    public enum class OpenPolicy(public val createIfMissing: Boolean, public val errorIfExists: Boolean) {
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
    public data class Options
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
             * EXPERIMENTAL: If true, append to existing MANIFEST and log files
             * when a database is opened.  This can significantly speed up open.
             *
             * Default: currently false, but may become true later.
            */
            val reuseLogs: Boolean = false,

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

            val loggerFactory: LoggerFactory = LoggerFactory.default,

            val trackClosableAllocation: Boolean = false,

            val failOnBadClose: Boolean = false

//            val comparator: ???
    ) {
        public companion object {
            public val DEFAULT: Options = Options()
        }
    }

    /**
     * Options that control read operations.
     */
    public data class ReadOptions(
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
        public companion object {
            public val DEFAULT: ReadOptions = ReadOptions()
        }
    }

    /**
     * Options that control write operations.
     */
    public data class WriteOptions(
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
        public companion object {
            public val DEFAULT: WriteOptions = WriteOptions()
        }
    }

}

