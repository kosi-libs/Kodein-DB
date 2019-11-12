package org.kodein.db.impl

import org.kodein.db.*
import org.kodein.db.impl.utils.kIsInstance
import org.kodein.db.model.ModelDB
import org.kodein.memory.Closeable
import kotlin.reflect.KClass

internal class DBImpl(override val mdb: ModelDB) : DB, DBReadModule, DBWriteModule, KeyMaker by mdb, Closeable by mdb {

    override fun newBatch(): Batch = BatchImpl(mdb.newBatch())

    override fun newSnapshot(vararg options: Options.Read): Snapshot = SnapshotImpl(mdb.newSnapshot(*options))

    override fun onAll(): DB.RegisterDsl<Any> = RegisterDslImpl(mdb, emptyList())

    override fun <M : Any> on(type: KClass<M>): DB.RegisterDsl<M> = RegisterDslImpl(mdb, listOf<(M) -> Boolean>({ type.kIsInstance(it) }))

}
