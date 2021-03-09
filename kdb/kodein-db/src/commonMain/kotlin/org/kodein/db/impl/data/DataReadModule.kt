package org.kodein.db.impl.data

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataRead
import org.kodein.db.from
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.*
import org.kodein.memory.text.readString
import org.kodein.memory.transfer
import org.kodein.memory.use

internal interface DataReadModule : DataKeyMakerModule, DataRead {

    val ldb: LevelDB
    val snapshot: LevelDB.Snapshot?

    override fun get(key: ReadMemory, vararg options: Options.Read): Allocation? = ldb.get(key, LevelDB.ReadOptions.from(snapshot, options))

    override fun findAll(vararg options: Options.Read): DataCursor = DataSimpleCursor(ldb, ldb.newCursor(LevelDB.ReadOptions.from(snapshot, options)), emptyDocumentPrefix)

    override fun findAllByType(type: Int, vararg options: Options.Read): DataCursor {
        val prefix = KBuffer.array(getDocumentKeySize(null)) { putDocumentKey(type, null) } .array
        ldb.newCursor(LevelDB.ReadOptions.from(snapshot, options)).transfer { cursor ->
            return DataSimpleCursor(ldb, cursor, prefix)
        }
    }

    override fun findById(type: Int, id: Value, isOpen: Boolean, vararg options: Options.Read): DataCursor {
        val prefix = KBuffer.array(getDocumentKeySize(id, isOpen)) { putDocumentKey(type, id, isOpen) } .array
        ldb.newCursor(LevelDB.ReadOptions.from(snapshot, options)).transfer { cursor ->
            return DataSimpleCursor(ldb, cursor, prefix)
        }
    }

    override fun findAllByIndex(type: Int, index: String, vararg options: Options.Read): DataCursor {
        val ro = LevelDB.ReadOptions.from(snapshot, options)
        val prefix = KBuffer.array(getIndexKeyStartSize(index, null)) { putIndexKeyStart(type, index, null) } .array
        ldb.newCursor(ro).transfer { cursor ->
            return DataIndexCursor(ldb, cursor, prefix, ro)
        }

    }

    override fun findByIndex(type: Int, index: String, value: Value, isOpen: Boolean, vararg options: Options.Read): DataCursor {
        val ro = LevelDB.ReadOptions.from(snapshot, options)
        val prefix = KBuffer.array(getIndexKeyStartSize(index, value, isOpen)) { putIndexKeyStart(type, index, value, isOpen) } .array
        ldb.newCursor(ro).transfer { cursor ->
            return DataIndexCursor(ldb, cursor, prefix, ro)
        }
    }

    override fun getIndexesOf(key: ReadMemory, vararg options: Options.Read): Set<String> {
        val indexes = SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            val refKey = it.newSlice { putRefKeyFromDocumentKey(key) }
            ldb.get(refKey, LevelDB.ReadOptions.from(snapshot, options)) ?: return emptySet()
        }

        val set = HashSet<String>()

        indexes.use {
            while (indexes.valid()) {
                val length = indexes.readInt()
                val indexKey = indexes.slice(indexes.position, length)
                indexes.skip(length)

                set.add(getIndexKeyName(indexKey).readString())
            }
        }

        return set
    }

}
