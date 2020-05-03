package org.kodein.db.impl.data

import org.kodein.db.*
import org.kodein.db.data.DataBatch
import org.kodein.db.impl.utils.putBody
import org.kodein.db.impl.utils.withLock
import org.kodein.memory.io.*
import org.kodein.memory.use
import org.kodein.memory.util.MaybeThrowable
import org.kodein.memory.util.forEachCatchTo

internal class DataBatchImpl(private val ddb: DataDBImpl) : DataKeyMakerModule, DataBatch {

    private val batch = ddb.ldb.newWriteBatch()

    private val deleteRefKeys = ArrayList<KBuffer>()

    private fun put(sb: SliceBuilder, body: Body, key: ReadMemory, indexes: Set<Index>): Int {
        val value = sb.newSlice { putBody(body) }
        batch.put(key, value)

        val refKey = KBuffer.array(key.size) { putRefKeyFromDocumentKey(key) }
        ddb.putIndexesInBatch(sb, batch, key, refKey, indexes)

        deleteRefKeys += refKey

        return value.available
    }

    override fun put(key: ReadMemory,  body: Body, indexes: Set<Index>, vararg options: Options.Write): Int {
        key.verifyDocumentKey()
        SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            return put(it, body, key, indexes)
        }
    }

    override fun delete(key: ReadMemory, vararg options: Options.Write) {
        key.verifyDocumentKey()
        batch.delete(key)
        val refKey = KBuffer.array(key.size) { putRefKeyFromDocumentKey(key) }
        deleteRefKeys += refKey
    }

    override fun write(afterErrors: MaybeThrowable, vararg options: Options.Write) {
        val checks = options.all<Anticipate>()
        val reacts = options.all<React>()
        use {
            checks.filter { it.needsLock.not() } .forEach { it.block() }
            ddb.ldb.newWriteBatch().use { fullBatch ->
                ddb.lock.withLock {
                    checks.filter { it.needsLock } .forEach { it.block() }
                    deleteRefKeys.forEach { ddb.deleteIndexesInBatch(fullBatch, it) }
                    fullBatch.append(batch)
                    ddb.ldb.write(fullBatch, DataDBImpl.toLdb(options))
                    reacts.filter { it.needsLock } .forEachCatchTo(afterErrors) { it.block(-1) }
                }
            }
        }
        reacts.filter { it.needsLock.not() } .forEachCatchTo(afterErrors) { it.block(-1) }
    }

    override fun close() = batch.close()
}
