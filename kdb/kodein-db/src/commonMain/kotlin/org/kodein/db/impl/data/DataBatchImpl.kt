package org.kodein.db.impl.data

import org.kodein.db.*
import org.kodein.db.data.DataBatch
import org.kodein.db.impl.Check
import org.kodein.db.impl.utils.putBody
import org.kodein.db.impl.utils.withLock
import org.kodein.memory.io.*
import org.kodein.memory.use

internal class DataBatchImpl(private val ddb: DataDBImpl) : DataKeyMakerModule, DataBatch {

    private val batch = ddb.ldb.newWriteBatch()

    private val deleteRefKeys = ArrayList<KBuffer>()

    val writeOptions = ArrayList<Options.Write>()

    private fun put(sb: SliceBuilder, body: Body, key: ReadBuffer, indexes: Set<Index>): Int {
        val value = sb.newSlice { putBody(body) }
        batch.put(key, value)

        val refKey = KBuffer.array(key.remaining) { putRefKeyFromObjectKey(key) }
        ddb.putIndexesInBatch(sb, batch, key, refKey, indexes)

        deleteRefKeys += refKey

        return value.remaining
    }

    override fun put(key: ReadBuffer,  body: Body, indexes: Set<Index>, vararg options: Options.Write): Int {
        key.verifyObjectKey()
        SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            return put(it, body, key, indexes)
        }
    }

    override fun delete(key: ReadBuffer, vararg options: Options.Write) {
        key.verifyObjectKey()
        batch.delete(key)
        val refKey = KBuffer.array(key.remaining) { putRefKeyFromObjectKey(key) }
        deleteRefKeys += refKey
    }

    override fun write(vararg options: Options.Write) {
        val allOptions = (writeOptions + options).toTypedArray()
        val checks = allOptions.all<Check>()
        use {
            ddb.ldb.newWriteBatch().use { fullBatch ->
                ddb.lock.withLock {
                    checks.forEach { it.block() }

                    deleteRefKeys.forEach { ddb.deleteIndexesInBatch(fullBatch, it) }

                    fullBatch.append(batch)

                    ddb.ldb.write(fullBatch, DataDBImpl.toLdb(allOptions))
                }
            }
        }
    }

    override fun addWriteOptions(vararg options: Options.Write) {
        writeOptions += options
    }

    override fun close() = batch.close()
}
