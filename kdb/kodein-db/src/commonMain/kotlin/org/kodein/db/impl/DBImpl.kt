package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.Options
import org.kodein.db.model.ModelDB
import kotlin.reflect.KClass

internal class DBImpl(override val mdb: ModelDB) : DB, BaseDBRead, BaseDBWrite {

    override fun newBatch(): DB.Batch {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun newSnapshot(vararg options: Options.Read): DB.Snapshot {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onAll(): DB.RegisterDsl<Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <M : Any> on(type: KClass<M>): DB.RegisterDsl<M> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}