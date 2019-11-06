package org.kodein.db.impl

import org.kodein.db.Batch
import org.kodein.db.KeyMaker
import org.kodein.db.Options
import org.kodein.db.model.ModelBatch
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable

class BatchImpl(override val mdb: ModelBatch) : Batch, DBWriteModule, KeyMaker by mdb, Closeable by mdb {
    override fun write(vararg options: Options.Write) {
        val afterErrors = MaybeThrowable()
        mdb.write(afterErrors, *options)
        afterErrors.shoot()
    }
}
