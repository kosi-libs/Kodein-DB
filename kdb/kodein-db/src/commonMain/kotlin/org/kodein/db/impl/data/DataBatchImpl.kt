package org.kodein.db.impl.data

import org.kodein.db.*
import org.kodein.db.data.DataBatch
import org.kodein.db.impl.utils.putBody
import org.kodein.db.impl.utils.withLock
import org.kodein.memory.io.*
import org.kodein.memory.use

internal class DataBatchImpl(private val ddb: DataDBImpl) : DataKeyMakerModule, DataBatch {

    private val batch = ddb.ldb.newWriteBatch()

    private val deleteRefKeys = ArrayList<KBuffer>()

    private fun put(sb: SliceBuilder, body: Body, key: ReadBuffer, indexes: Set<Index>): Int {
        val value = sb.newSlice { putBody(body) }
        batch.put(key, value)

        val refKey = KBuffer.array(key.remaining) { putRefKeyFromObjectKey(key) }
        ddb.putIndexesInBatch(sb, batch, key, refKey, indexes)

        deleteRefKeys += refKey

        return value.remaining
    }

    override fun put(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, vararg options: Options.Write): Int {
        SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            val key = it.newSlice { putObjectKey(type, primaryKey) }
            return put(it, body, key, indexes)
        }
    }

    override fun put(type: String, primaryKey: Value, key: ReadBuffer,  body: Body, indexes: Set<Index>, vararg options: Options.Write): Int {
        SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            try {
                VerificationWriteable(key.duplicate()).putObjectKey(type, primaryKey)
            } catch (_: VerificationWriteable.DiffException) {
                return -1
            }

            return put(it, body, key, indexes)
        }
    }

    private fun putAndSetKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, key: KBuffer): Int {
        key.putObjectKey(type, primaryKey)
        key.flip()

        SliceBuilder.native(DataDBImpl.DEFAULT_CAPACITY).use {
            return put(it, body, key, indexes)
        }
    }

    override fun putAndGetHeapKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, vararg options: Options.Write): Sized<KBuffer> {
        val key = KBuffer.array(getObjectKeySize(type, primaryKey))
        val length = putAndSetKey(type, primaryKey, body, indexes, key)
        return Sized(key, length)
    }

    override fun putAndGetNativeKey(type: String, primaryKey: Value, body: Body, indexes: Set<Index>, vararg options: Options.Write): Sized<Allocation> {
        val key = Allocation.native(getObjectKeySize(type, primaryKey))
        val length = putAndSetKey(type, primaryKey, body, indexes, key)
        return Sized(key, length)
    }

    override fun delete(key: ReadBuffer, vararg options: Options.Write) {
        batch.delete(key)
        val refKey = KBuffer.array(key.remaining) { putRefKeyFromObjectKey(key) }
        deleteRefKeys += refKey
    }

    override fun write(vararg options: Options.Write) {
        use {
            ddb.ldb.newWriteBatch().use { fullBatch ->
                ddb.lock.withLock {
                    for (refKey in deleteRefKeys) {
                        ddb.deleteIndexesInBatch(fullBatch, refKey)
                    }

                    fullBatch.append(batch)

                    ddb.ldb.write(fullBatch, DataDBImpl.toLdb(options))
                }
            }
        }
    }

    override fun close() = batch.close()
}
