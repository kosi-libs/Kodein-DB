package org.kodein.db

import org.kodein.memory.Closeable

interface AsyncSnapshot : AsyncDBRead, Closeable {
    override fun sync(): Snapshot
}
