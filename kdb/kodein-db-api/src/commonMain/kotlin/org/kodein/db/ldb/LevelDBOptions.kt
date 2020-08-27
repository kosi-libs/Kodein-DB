package org.kodein.db.ldb

import org.kodein.db.Options
import org.kodein.db.leveldb.LevelDB
import org.kodein.log.LoggerFactory


@Suppress("unused")
public sealed class LevelDBOptions(internal val transform: LevelDB.Options.() -> LevelDB.Options) : Options.Open {

    /**
     * Defines how to react if the database exists or not.
     *
     * (Default: OpenPolicy.OPEN_OR_CREATE)
     *
     * @see OpenPolicy
     */
    public data class OpenPolicy(val openPolicy: LevelDB.OpenPolicy): LevelDBOptions({ copy(openPolicy = openPolicy) })

    /**
     * If true, the implementation will do aggressive checking of the data it is processing and will stop early if it detects any errors.
     *
     * This may have unforeseen ramifications: for example, a corruption of one DB entry may cause a large number of entries to become unReadBuffer or for the entire DB to become unopenable.
     *
     * (Default: false)
     */
    public data class ParanoidChecks(val paranoidChecks: Boolean): LevelDBOptions({ copy(paranoidChecks = paranoidChecks) })

    /**
     * If true, the LevelDB implementation will print internal logs.
     *
     * This severly slows the database and should never be set in production.
     *
     * (Default: false)
     */
    public data class PrintLogs(val printLogs: Boolean = false): LevelDBOptions({ copy(printLogs = printLogs) })

    /**
     * Amount of data to build up in memory (backed by an unsorted log on disk) before converting to a sorted on-disk file.
     *
     * Larger values increase performance, especially during bulk loads.
     * Up to two write buffers may be held in memory at the same time, so you may wish to adjust this parameter to control memory usage.
     * Also, a larger write buffer will result in a longer recovery time the next time the database is opened.
     *
     * (Default: 4MB)
     */
    public data class WriteBufferSize(val writeBufferSize: Int): LevelDBOptions({ copy(writeBufferSize = writeBufferSize) })

    /**
     * Number of open files that can be used by the DB.
     *
     * You may need to increase this if your database has a large working set (budget one open file per 2MB of working set).
     *
     * (Default: 1000)
     */
    public data class MaxOpenFiled(val maxOpenFiles: Int): LevelDBOptions({ copy(maxOpenFiles = maxOpenFiles) })

    /**
     * Size of the LRU cache LevelDB will use to prevent unneeded disk access.
     *
     * (Default: 8MB)
     */
    public data class CacheSize(val cacheSize: Int): LevelDBOptions({ copy(cacheSize = cacheSize) })

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
    public data class BlockSize(val blockSize: Int): LevelDBOptions({ copy(blockSize = blockSize) })

    /**
     * Number of keys between restart points for delta encoding of keys.
     *
     * Most clients should leave this parameter alone.
     *
     * This parameter can be changed dynamically.
     *
     * (Default: 16)
     */
    public data class BlockRestartInterval(val blockRestartInterval: Int): LevelDBOptions({ copy(blockRestartInterval = blockRestartInterval) })

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
    public data class MaxFileSize(val maxFileSize: Int): LevelDBOptions({ copy(maxFileSize = maxFileSize) })

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
    public data class SnappyCompression(val snappyCompression: Boolean): LevelDBOptions({ copy(snappyCompression = snappyCompression) })

    /**
     * EXPERIMENTAL: If true, append to existing MANIFEST and log files
     * when a database is opened.  This can significantly speed up open.
     *
     * Default: currently false, but may become true later.
     */
    public data class ReuseLogs(val reuseLogs: Boolean): LevelDBOptions({ copy(reuseLogs = reuseLogs) })

    /**
     * If non-0, use a Bloom filter policy to reduce disk reads.
     *
     * Uses a bloom filter with approximately the specified number of bits per key.
     * A good value is 10, which yields a filter with ~ 1% false positive rate.
     *
     * (Default: 10)
     */
    public data class BloomFilterBitsPerKey(val bloomFilterBitsPerKey: Int): LevelDBOptions({ copy(bloomFilterBitsPerKey = bloomFilterBitsPerKey) })

    /**
     * If a DB cannot be opened, you may attempt to set this to true to resurrect as much of the contents of the database as possible.
     *
     * Some data may be lost, so be careful when setting this on a database that contains important information.
     */
    public data class RepairOnCorruption(val repairOnCorruption: Boolean): LevelDBOptions({ copy(repairOnCorruption = repairOnCorruption) })

    public companion object {
        public fun new(options: Array<out Options.Open>): LevelDB.Options = options.filterIsInstance<LevelDBOptions>().fold(LevelDB.Options.DEFAULT) { l, o -> o.transform(l) }
    }

}

public data class DBLoggerFactory(val loggerFactory: LoggerFactory): LevelDBOptions({ copy(loggerFactory = loggerFactory) })

public data class TrackClosableAllocation(val trackClosableAllocation: Boolean): LevelDBOptions({ copy(trackClosableAllocation = trackClosableAllocation) })

public data class FailOnBadClose(val failOnBadClose: Boolean): LevelDBOptions({ copy(failOnBadClose = failOnBadClose) })
