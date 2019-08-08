package org.kodein.db.impl

import org.kodein.db.DB
import org.kodein.db.Options
import org.kodein.db.model.ModelDB

class BatchImpl(override val mdb: ModelDB.Batch) : DB.Batch, BaseDBWrite {

    override fun write(vararg options: Options.Write) = mdb.write(*options)

    override fun close() = mdb.close()

}
