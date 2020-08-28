package org.kodein.db.impl.model.cache

import org.kodein.db.*
import org.kodein.db.model.ModelBatch
import org.kodein.memory.Closeable
import org.kodein.memory.util.MaybeThrowable
import org.kodein.memory.util.forEachCatchTo
import kotlin.reflect.KClass

internal class CachedModelBatch(private val cmdb: CachedModelDB, private val batch: ModelBatch) : ModelBatch, KeyMaker by batch, Closeable by batch {

    private val reacts = ArrayList<CachedModelDB.() -> Unit>()

    override fun <M : Any> put(model: M, vararg options: Options.Write): KeyAndSize<M> {
        val key = keyFrom(model)
        val size = batch.put(key, model, *options)
        reacts.add { didPut(model, key, size, options) }
        return KeyAndSize(key, size)
    }

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Write): Int {
        val size = batch.put(key, model, *options)
        reacts.add { didPut(model, key, size, options) }
        return size
    }

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write) {
        batch.delete(type, key, *options)
        reacts.add { didDelete(key, options) }
    }

    override fun write(afterErrors: MaybeThrowable, vararg options: Options.Write) {
        batch.write(afterErrors, *(options + React(true) {
            reacts.forEachCatchTo(afterErrors) { cmdb.it() }
        }))
    }

}
