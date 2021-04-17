package org.kodein.db.impl.data

import org.kodein.db.*
import org.kodein.db.data.DataBatch
import org.kodein.db.impl.utils.withLock
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.*
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import org.kodein.memory.util.forEachCatchTo

internal class DataBatchImpl(private val ddb: DataDBImpl) : DataKeyMakerModule, DataBatch {

    private val batch = ddb.ldb.newWriteBatch()

    private val deleteRefKeys = ArrayList<Memory>()

    private fun put(sb: SliceBuilder, body: Body, key: ReadMemory, indexes: Map<String, Value>): Int {
        val value = sb.slice { writeBody(body) }
        batch.put(key, value)

        val refKey = Memory.array(key.size) { writeRefKeyFromDocumentKey(key) }
        ddb.putIndexesInBatch(sb, batch, key, refKey, indexes)

        deleteRefKeys += refKey

        return value.size
    }

    override fun put(key: ReadMemory,  body: Body, indexes: Map<String, Value>, vararg options: Options.Write): Int {
        key.verifyDocumentKey()
        SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            return put(it, body, key, indexes)
        }
    }

    override fun delete(key: ReadMemory, vararg options: Options.Write) {
        key.verifyDocumentKey()
        batch.delete(key)
        val refKey = Memory.array(key.size) { writeRefKeyFromDocumentKey(key) }
        deleteRefKeys += refKey
    }

    override fun write(afterErrors: MaybeThrowable, vararg options: Options.Write) {
        val anticipations = options.all<Anticipate>()
        val inLockAnticipations = options.all<AnticipateInLock>()
        val inLockReactions = options.all<ReactInLock>()
        val reactions = options.all<React>()

        this.use {
            anticipations.forEach { it.block() }
            ddb.ldb.newWriteBatch().use { fullBatch ->
                ddb.lock.withLock {
                    inLockAnticipations.forEach { it.block(batch) }
                    deleteRefKeys.forEach { ddb.deleteIndexesInBatch(fullBatch, it) }
                    fullBatch.append(batch)
                    ddb.ldb.write(fullBatch, LevelDB.WriteOptions.from(options))
                    inLockReactions.forEachCatchTo(afterErrors) { it.block(-1) }
                }
            }
        }
        reactions.forEachCatchTo(afterErrors) { it.block(-1) }
    }

    override fun close() = batch.close()
}
