package org.kodein.db.data

import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.memory.io.ReadMemory

public interface DataWrite : DataKeyMaker {

    public fun put(key: ReadMemory, body: Body, indexes: Set<Index> = emptySet(), vararg options: Options.Write): Int

    public fun delete(key: ReadMemory, vararg options: Options.Write)

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
    public data class Sync(val sync: Boolean = false) : Options.Write

}
