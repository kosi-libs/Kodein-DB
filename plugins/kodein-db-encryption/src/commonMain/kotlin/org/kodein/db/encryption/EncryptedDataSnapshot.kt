package org.kodein.db.encryption

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.data.DataCursor
import org.kodein.db.data.DataIndexCursor
import org.kodein.db.data.DataKeyMaker
import org.kodein.db.data.DataSnapshot
import org.kodein.db.invoke
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadAllocation
import org.kodein.memory.io.ReadMemory


internal class EncryptedDataSnapshot(override val eddb: EncryptedDataDB, override val data: DataSnapshot)
    : DataSnapshot, EncryptedDataReadModule, DataKeyMaker by eddb, Closeable by data
