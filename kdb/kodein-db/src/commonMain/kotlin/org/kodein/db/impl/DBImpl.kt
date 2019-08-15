package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.Options
import org.kodein.db.model.ModelDB
import kotlin.reflect.KClass

internal class DBImpl(override val mdb: ModelDB) : DB, BaseDBRead, BaseDBWrite {

    override fun newBatch(): DB.Batch = BatchImpl(mdb.newBatch())

    override fun newSnapshot(vararg options: Options.Read): DB.Snapshot = SnapshotImpl(mdb.newSnapshot(*options))

    override fun onAll(): DB.RegisterDsl<Any> = RegisterDslImpl(mdb, emptyList())

    override fun <M : Any> on(type: KClass<M>): DB.RegisterDsl<M> = RegisterDslImpl(mdb, listOf<(M) -> Boolean>({ type.isInstance(it) }))

    override fun close() = mdb.close()

}
