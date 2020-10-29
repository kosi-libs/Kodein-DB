package org.kodein.db.leveldb.inmemory

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBException
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.memory.io.ReadMemory
import kotlin.native.concurrent.ThreadLocal


@ThreadLocal
public object LevelDBInMemory : LevelDBFactory {
    private class IMData {
        val data = HashMap<ReadMemory, ByteArray>()
        var isOpen: Boolean = false
    }

    private val dbs = HashMap<String, IMData>()

    override fun open(path: String, options: LevelDB.Options): LevelDB {
        val data = when (options.openPolicy) {
            LevelDB.OpenPolicy.OPEN_OR_CREATE -> dbs.getOrPut(path) { IMData() }
            LevelDB.OpenPolicy.OPEN -> dbs[path] ?: throw LevelDBException("No db at path $path")
            LevelDB.OpenPolicy.CREATE -> {
                if (path in dbs) throw LevelDBException("Db already exist at path $path")
                IMData().also { dbs[path] = it }
            }
        }
        if (data.isOpen) throw LevelDBException("Db at path $path is already open")
        data.isOpen = true
        return InMemoryLevelDB(
            data = data.data,
            options = options,
            onClose = { data.isOpen = false }
        )
    }

    override fun destroy(path: String, options: LevelDB.Options) {
        dbs.remove(path)
    }

}

public val LevelDB.Companion.inMemory: LevelDBFactory get() = LevelDBInMemory
