package org.kodein.db.model

import org.kodein.db.DBListener
import org.kodein.db.ExtensionKey
import org.kodein.db.Options
import org.kodein.db.data.DataDB
import org.kodein.db.data.DataSnapshot
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable

public interface ModelDB : ModelWrite, ModelRead, Closeable {

    public fun newBatch(): ModelBatch

    public fun newSnapshot(vararg options: Options.Read): ModelSnapshot

    public fun register(listener: DBListener<Any>): Closeable

    public fun <T: Any> getExtension(key: ExtensionKey<T>): T?

    public val data: DataDB

    public companion object
}
