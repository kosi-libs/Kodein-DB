package org.kodein.db.impl

import org.kodein.db.Batch
import org.kodein.db.KeyMaker
import org.kodein.db.Options
import org.kodein.db.model.ModelBatch
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable

public class BatchImpl(override val mdb: ModelBatch) : Batch, DBWriteModule, KeyMaker by mdb, Closeable by mdb {
    private val batchOptions = ArrayList<Options.Write>()

    override fun write(vararg options: Options.Write) {
        val afterErrors = MaybeThrowable()
        mdb.write(afterErrors, *(batchOptions + options).toTypedArray())
        afterErrors.shoot()
    }

    override fun addOptions(vararg options: Options.Write) { batchOptions.addAll(options) }
}
