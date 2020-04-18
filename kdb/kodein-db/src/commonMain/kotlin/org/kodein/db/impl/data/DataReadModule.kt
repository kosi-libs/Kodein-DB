package org.kodein.db.impl.data

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.ascii.getAscii
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

    override fun get(key: ReadMemory, vararg options: Options.Read): Allocation? = ldb.get(key, toLdb(options))

    override fun findAll(vararg options: Options.Read): DataCursor = DataSimpleCursor(ldb, ldb.newCursor(toLdb(options)), emptyDocumentPrefix)

    override fun findAllByType(type: Int, vararg options: Options.Read): DataCursor {
        val prefix = KBuffer.array(getDocumentKeySize(null)) { putDocumentKey(type, null) } .array
        ldb.newCursor(toLdb(options)).transfer { cursor ->
            return DataSimpleCursor(ldb, cursor, prefix)
        }
    }

    override fun findById(type: Int, id: Value, isOpen: Boolean, vararg options: Options.Read): DataCursor {
        val prefix = KBuffer.array(getDocumentKeySize(id, isOpen)) { putDocumentKey(type, id, isOpen) } .array
        ldb.newCursor(toLdb(options)).transfer { cursor ->
            return DataSimpleCursor(ldb, cursor, prefix)
        }
    }

    override fun findAllByIndex(type: Int, index: String, vararg options: Options.Read): DataCursor {
        val ro = toLdb(options)
        val prefix = KBuffer.array(getIndexKeyStartSize(index, null)) { putIndexKeyStart(type, index, null) } .array
        ldb.newCursor(ro).transfer { cursor ->
            return DataIndexCursor(ldb, cursor, prefix, ro)
        }

    }

    override fun findByIndex(type: Int, index: String, value: Value, isOpen: Boolean, vararg options: Options.Read): DataCursor {
        val ro = toLdb(options)
        val prefix = KBuffer.array(getIndexKeyStartSize(index, value, isOpen)) { putIndexKeyStart(type, index, value, isOpen) } .array
        ldb.newCursor(ro).transfer { cursor ->
            return DataIndexCursor(ldb, cursor, prefix, ro)
        }
    }

    override fun getIndexesOf(key: ReadMemory, vararg options: Options.Read): Set<String> {
        val indexes = SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            val refKey = it.newSlice { putRefKeyFromDocumentKey(key) }
            ldb.get(refKey, toLdb(options)) ?: return emptySet()
        }

        val set = HashSet<String>()

        indexes.use {
            while (indexes.valid()) {
                val length = indexes.readInt()
                val indexKey = indexes.slice(indexes.position, length)
                indexes.skip(length)

                val name = getIndexKeyName(indexKey)
                set.add(name.getAscii())
            }
        }

        return set
    }

}
