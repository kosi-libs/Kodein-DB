package org.kodein.db.data

import org.kodein.db.Body
import org.kodein.db.Index
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.Sized
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadBuffer

interface DataWrite : DataKeyMaker {

    fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), vararg options: Options.Write): Int

    fun putAndGetHeapKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), vararg options: Options.Write): Sized<KBuffer>

    fun putAndGetNativeKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index> = emptySet(), vararg options: Options.Write): Sized<Allocation>

    fun delete(key: ReadBuffer, vararg options: Options.Write)

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
    data class Sync(val sync: Boolean = false) : Options.Write

}
