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
import org.kodein.memory.util.deferScope

internal interface DataReadModule : DataKeyMakerModule, DataRead {

    val ldb: LevelDB
    val snapshot: LevelDB.Snapshot?

    override fun get(key: ReadMemory, vararg options: Options.Read): Allocation? = ldb.get(key, LevelDB.ReadOptions.from(snapshot, options))

    override fun findAll(vararg options: Options.Read): DataCursor = DataSimpleCursor(ldb, ldb.newCursor(LevelDB.ReadOptions.from(snapshot, options)), emptyDocumentPrefix)

    override fun findAllByType(type: Int, vararg options: Options.Read): DataCursor {
        val prefix = Memory.array(getDocumentKeySize(null)) { writeDocumentKey(type, null) } .array
        ldb.newCursor(LevelDB.ReadOptions.from(snapshot, options)).transfer { cursor ->
            return DataSimpleCursor(ldb, cursor, prefix)
        }
    }

    override fun findById(type: Int, id: Value, isOpen: Boolean, vararg options: Options.Read): DataCursor {
        val prefix = Memory.array(getDocumentKeySize(id, isOpen)) { writeDocumentKey(type, id, isOpen) } .array
        ldb.newCursor(LevelDB.ReadOptions.from(snapshot, options)).transfer { cursor ->
            return DataSimpleCursor(ldb, cursor, prefix)
        }
    }

    override fun findAllByIndex(type: Int, index: String, vararg options: Options.Read): DataCursor {
        val ro = LevelDB.ReadOptions.from(snapshot, options)
        val prefix = Memory.array(getIndexKeyStartSize(index, null)) { writeIndexKeyStart(type, index, null) } .array
        ldb.newCursor(ro).transfer { cursor ->
            return DataIndexCursor(ldb, cursor, prefix, ro)
        }

    }

    override fun findByIndex(type: Int, index: String, value: Value, isOpen: Boolean, vararg options: Options.Read): DataCursor {
        val ro = LevelDB.ReadOptions.from(snapshot, options)
        val prefix = Memory.array(getIndexKeyStartSize(index, value, isOpen)) { writeIndexKeyStart(type, index, value, isOpen) } .array
        ldb.newCursor(ro).transfer { cursor ->
            return DataIndexCursor(ldb, cursor, prefix, ro)
        }
    }

    override fun getIndexesOf(key: ReadMemory, vararg options: Options.Read): Set<String> {
        deferScope {
            val refKey = Allocation.native(key.size) { writeRefKeyFromDocumentKey(key) } .useInScope()
            val indexes = ldb.get(refKey, LevelDB.ReadOptions.from(snapshot, options))?.useInScope()?.asReadable() ?: return emptySet()

            val set = HashSet<String>()

            while (indexes.valid()) {
                val length = indexes.readInt()
                val indexKey = indexes.readMemory(length)
                set.add(getIndexKeyName(indexKey).readString())
            }
            return set
        }
    }

}
