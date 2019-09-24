package org.kodein.db

import org.kodein.memory.Closeable


interface AsyncBatch : AsyncDBWrite, Closeable {
    suspend fun write(vararg options: Options.Write)

    override fun sync(): Batch
}
