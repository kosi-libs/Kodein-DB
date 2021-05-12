package org.kodein.db.impl

import org.kodein.db.Batch
import org.kodein.db.Key
import org.kodein.db.KeyMaker
import org.kodein.db.Options
import org.kodein.db.model.ModelBatch
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable
import kotlin.reflect.KClass

public class BatchImpl(private val mdb: ModelBatch) : Batch, KeyMaker by mdb, Closeable by mdb {

    override fun <M : Any> put(model: M, vararg options: Options.BatchPut): Key<M> =
        mdb.put(model, *options).key

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.BatchPut) {
        mdb.put(key, model, *options)
    }

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.BatchDelete) {
        mdb.delete(type, key, *options)
    }

    override fun write(vararg options: Options.BatchWrite) {
        val afterErrors = MaybeThrowable()
        mdb.write(afterErrors, *options)
        afterErrors.shoot()
    }
}
