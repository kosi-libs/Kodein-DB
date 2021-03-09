package org.kodein.db.plugin.fts

import org.kodein.db.*
import org.kodein.db.data.DataDB
import org.kodein.db.model.ModelBatch
import org.kodein.db.model.ModelDB
import org.kodein.db.model.ModelRead
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.use
import org.kodein.memory.util.deferScope
import kotlin.reflect.KClass


internal class FtsModelDB(private val modelDB: ModelDB) : ModelDB, ModelRead by modelDB, Closeable by modelDB {

    internal val levelDB get() = modelDB.data.ldb

    private fun <T> putFullText(model: Any, key: ReadMemory, options: Array<out Options.Write>, putModel: ModelDB.(Array<out Options.Write>) -> T): T {
        deferScope {
            if (model is HasFullText) {
                val batchUtil = FtsBatchUtil().useInScope()
                val putBatch = levelDB.newWriteBatch().useInScope()
                batchUtil.putTokens(model.texts(), key, putBatch)
                return modelDB.putModel(options + AnticipateInLock { fullBatch ->
                    batchUtil.deleteTokens(key, levelDB, fullBatch)
                    fullBatch.append(putBatch)
                })
            } else {
                val batchUtil = FtsBatchUtilKeys().useInScope()
                return modelDB.putModel(options + AnticipateInLock { fullBatch ->
                    batchUtil.deleteTokens(key, levelDB, fullBatch)
                })
            }
        }
    }

    override fun <M : Any> put(model: M, vararg options: Options.Write): KeyAndSize<M> {
        val key = keyFrom(model, *options).bytes

        return putFullText(model, key, options) { put(model, *it) }
    }

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Write): Int {
        if (model !is HasFullText) return modelDB.put(key, model, *options)

        return putFullText(model, key.bytes, options) { put(key, model, *it) }
    }

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write) {
        FtsBatchUtilKeys().use { batchUtil ->
            modelDB.delete(type, key, *(options + AnticipateInLock { writeBatch ->
                batchUtil.deleteTokens(key.bytes, levelDB, writeBatch)
            }))
        }
    }

    override fun newBatch(): ModelBatch = FtsModelBatch(modelDB.newBatch(), levelDB)

    override fun newSnapshot(vararg options: Options.Read) = modelDB.newSnapshot()

    override fun register(listener: DBListener<Any>) = modelDB.register(listener)

    override val data: DataDB get() = modelDB.data

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getExtension(key: ExtensionKey<T>): T? {
        if (key == FtsQuery) return FtsQuery(this) as T
        return modelDB.getExtension(key)
    }
}
