package org.kodein.db.data

import org.kodein.db.Options

interface DataOptions : Options {

    data class Read(
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
            val fillCache: Boolean = true
    ) : DataOptions, Options.Read

    data class Write(
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
    ) : DataOptions, Options.Write

}
