package org.kodein.db.impl.data

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataIndexCursor
import org.kodein.db.data.DataRead
import org.kodein.db.kv.KeyValueRead
import org.kodein.db.kv.KeyValueSnapshot
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.*
import org.kodein.memory.transfer
import org.kodein.memory.util.deferScope

internal interface DataReadModule : DataKeyMakerModule, DataRead {

    val kv: KeyValueRead

    fun currentOrNewSnapshot(): Pair<KeyValueSnapshot, Boolean>

    override fun get(key: ReadMemory, vararg options: Options.Get): Allocation? = kv.get(key, *options)

    override fun findAll(vararg options: Options.Find): DataCursor = DataCursorImpl(kv.newCursor(*options), emptyDocumentPrefix)

    override fun findAllByType(type: Int, vararg options: Options.Find): DataCursor {
        val prefix = Memory.array(getDocumentKeySize(null)) { writeDocumentKey(type, null) } .array
        kv.newCursor(*options).transfer { cursor ->
            return DataCursorImpl(cursor, prefix)
        }
    }

    override fun findById(type: Int, id: Value, isOpen: Boolean, vararg options: Options.Find): DataCursor {
        val prefix = Memory.array(getDocumentKeySize(id, isOpen)) { writeDocumentKey(type, id, isOpen) } .array
        kv.newCursor(*options).transfer { cursor ->
            return DataCursorImpl(cursor, prefix)
        }
    }

    override fun findAllByIndex(type: Int, index: String, vararg options: Options.Find): DataIndexCursor {
        val prefix = Memory.array(getIndexKeyStartSize(index, null)) { writeIndexKeyStart(type, index, null) } .array

        val (snapshot, closeSnapshot) = currentOrNewSnapshot()
        snapshot.newCursor(*options).transfer { cursor ->
            return DataIndexCursorImpl(
                snapshot, closeSnapshot,
                cursor, prefix,
                options.filterIsInstance<Options.Get>().toTypedArray()
            )
        }

    }

    override fun findByIndex(type: Int, index: String, value: Value, isOpen: Boolean, vararg options: Options.Find): DataIndexCursor {
        val prefix = Memory.array(getIndexKeyStartSize(index, value, isOpen)) { writeIndexKeyStart(type, index, value, isOpen) } .array
        val (snapshot, closeSnapshot) = currentOrNewSnapshot()
        snapshot.newCursor(*options).transfer { cursor ->
            return DataIndexCursorImpl(
                snapshot, closeSnapshot,
                cursor, prefix,
                options.filterIsInstance<Options.Get>().toTypedArray()
            )
        }
    }

    override fun getIndexesOf(key: ReadMemory): Set<String> {
        deferScope {
            val refKey = Allocation.native(key.size) { writeRefKeyFromDocumentKey(key) } .useInScope()
            val refBody = kv.get(refKey)?.useInScope() ?: return emptySet()

            return getRefBodyIndexNames(refBody)
        }
    }

}
