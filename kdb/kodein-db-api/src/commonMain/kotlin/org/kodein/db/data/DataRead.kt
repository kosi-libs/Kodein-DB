package org.kodein.db.data

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.ReadMemory

interface DataRead : DataKeyMaker {

    fun get(key: ReadMemory, vararg options: Options.Read): Allocation?

    fun findAll(vararg options: Options.Read): DataCursor

    fun findAllByType(type: Int, vararg options: Options.Read): DataCursor

    fun findById(type: Int, id: Value, isOpen: Boolean = false, vararg options: Options.Read): DataCursor

    fun findAllByIndex(type: Int, index: String, vararg options: Options.Read): DataCursor

    fun findByIndex(type: Int, index: String, value: Value, isOpen: Boolean = false, vararg options: Options.Read): DataCursor

    fun getIndexesOf(key: ReadMemory, vararg options: Options.Read): List<String>

    /**
     * If true, all data read from underlying storage will be verified against corresponding checksums.
     *
     * (Default: false)
     */
    data class VerifyChecksum(val verifyChecksums: Boolean) : Options.Read

    /**
     * Should the data read for this iteration be cached in memory?
     *
     * Callers may wish to set this field to false for bulk scans.
     *
     * (Default: true)
     */
    data class FillCache(val fillCache: Boolean) : Options.Read

}
