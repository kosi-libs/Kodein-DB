package org.kodein.db.impl.data

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.ascii.readAscii
import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataRead
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.*
import org.kodein.memory.transfer
import org.kodein.memory.use

internal interface DataReadModule : DataKeyMakerModule, DataRead {

    val ldb: LevelDB
    val snapshot: LevelDB.Snapshot?

    private fun toLdb(options: Array<out Options.Read>): LevelDB.ReadOptions =
            options.fold(
                snapshot?.let { LevelDB.ReadOptions(snapshot = it) } ?: LevelDB.ReadOptions.DEFAULT
            ) { l, o ->
                when (o) {
                    is DataRead.VerifyChecksum -> l.copy(verifyChecksums = o.verifyChecksums)
                    is DataRead.FillCache -> l.copy(fillCache = o.fillCache)
                    else -> l
                }
            }

    override fun get(key: ReadBuffer, vararg options: Options.Read): Allocation? = ldb.get(key, toLdb(options))

    override fun findAll(vararg options: Options.Read): DataCursor = DataSimpleCursor(ldb.newCursor(toLdb(options)), objectEmptyPrefix.asManagedAllocation())

    override fun findAllByType(type: String, vararg options: Options.Read): DataCursor {
        Allocation.native(getObjectKeySize(type, null)) { putObjectKey(type, null) } .transfer { key ->
            ldb.newCursor(toLdb(options)).transfer { cursor ->
                return DataSimpleCursor(cursor, key)
            }
        }
    }

    override fun findById(type: String, id: Value, isOpen: Boolean, vararg options: Options.Read): DataCursor {
        Allocation.native(getObjectKeySize(type, id, isOpen)) { putObjectKey(type, id, isOpen) } .transfer { key ->
            ldb.newCursor(toLdb(options)).transfer { cursor ->
                return DataSimpleCursor(cursor, key)
            }
        }
    }

    override fun findAllByIndex(type: String, index: String, vararg options: Options.Read): DataCursor {
        val ro = toLdb(options)
        Allocation.native(getIndexKeyStartSize(type, index, null)) { putIndexKeyStart(type, index, null) } .transfer { key ->
            ldb.newCursor(ro).transfer { cursor ->
                return DataIndexCursor(ldb, cursor, key, ro)
            }
        }
    }

    override fun findByIndex(type: String, index: String, value: Value, isOpen: Boolean, vararg options: Options.Read): DataCursor {
        val ro = toLdb(options)
        Allocation.native(getIndexKeyStartSize(type, index, value, isOpen)) { putIndexKeyStart(type, index, value, isOpen) } .transfer { key ->
            ldb.newCursor(ro).transfer { cursor ->
                return DataIndexCursor(ldb, cursor, key, ro)
            }
        }
    }

    override fun getIndexesOf(key: ReadBuffer, vararg options: Options.Read): List<String> {
        val indexes = SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            val refKey = it.newSlice { putRefKeyFromObjectKey(key) }
            ldb.get(refKey, toLdb(options)) ?: return emptyList()
        }

        val list = ArrayList<String>()

        indexes.use {
            while (indexes.hasRemaining()) {
                val length = indexes.readInt()
                val indexKey = indexes.slice(indexes.position, length)
                indexes.skip(length)

                val type = getIndexKeyName(indexKey)
                list.add(type.readAscii())
            }
        }

        return list
    }

}