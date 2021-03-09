package org.kodein.db.plugin.fts

import org.kodein.db.*
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.model.ModelBatch
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.getBytes
import org.kodein.memory.io.wrap
import org.kodein.memory.util.MaybeThrowable
import kotlin.reflect.KClass


internal class FtsModelBatch(private val modelBatch: ModelBatch, private val levelDB: LevelDB) : ModelBatch, KeyMaker by modelBatch, ValueMaker by modelBatch {

    private val toDelete = ArrayList<ByteArray>()
    private var batchUtil: FtsBatchUtil? = null
    private var putBatch: LevelDB.WriteBatch? = null

    private fun putFullText(model: Any, key: ReadMemory) {
        toDelete.add(key.getBytes(0))

        if (model is HasFullText) {
            if (putBatch == null) putBatch = levelDB.newWriteBatch()
            if (batchUtil == null) batchUtil = FtsBatchUtil()
            batchUtil!!.putTokens(model.texts(), key, putBatch!!)
        }
    }

    override fun <M : Any> put(model: M, vararg options: Options.Write): KeyAndSize<M> {
        putFullText(model, keyFrom(model, *options).bytes)
        return modelBatch.put(model, *options)
    }

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Write): Int {
        putFullText(model, key.bytes)
        return modelBatch.put(key, model, *options)
    }

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write) {
        toDelete.add(key.bytes.getBytes(0))
        modelBatch.delete(type, key, *options)
    }

    override fun write(afterErrors: MaybeThrowable, vararg options: Options.Write) {
        modelBatch.write(afterErrors, *(options + AnticipateInLock { fullBatch ->
            if (toDelete.isNotEmpty()) {
                if (batchUtil == null) batchUtil = FtsBatchUtil()
                toDelete.forEach { key ->
                    batchUtil!!.deleteTokens(KBuffer.wrap(key), levelDB, fullBatch)
                }
            }
            if (putBatch != null) fullBatch.append(putBatch!!)
        }))
    }

    override fun close() {
        putBatch?.close()
        batchUtil?.close()
        modelBatch.close()
    }
}
