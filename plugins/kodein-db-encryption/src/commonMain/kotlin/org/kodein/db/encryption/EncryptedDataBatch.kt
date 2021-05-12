package org.kodein.db.encryption

import org.kodein.db.Body
import org.kodein.db.Options
import org.kodein.db.data.DataBatch
import org.kodein.db.data.DataIndexMap
import org.kodein.db.data.DataKeyMaker
import org.kodein.db.invoke
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.util.MaybeThrowable


internal class EncryptedDataBatch(private val eddb: EncryptedDataDB, private val batch: DataBatch)
    : DataBatch, DataKeyMaker by eddb, Closeable by batch {

    override fun put(key: ReadMemory, body: Body, indexes: DataIndexMap, vararg options: Options.BatchPut): Int {
        val (newBody, newIndexes, aesKey) = eddb.encrypt(key, body, indexes, options(), options<IV>()?.iv)
        val size = batch.put(key, newBody, newIndexes, *options)
        aesKey?.fill(0)
        return size
    }

    override fun delete(key: ReadMemory, vararg options: Options.BatchDelete) {
        batch.delete(key, *options)
    }

    override fun write(afterErrors: MaybeThrowable, vararg options: Options.BatchWrite) {
        batch.write(afterErrors, *options)
    }

}
