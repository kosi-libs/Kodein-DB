package org.kodein.db.impl.kv

import org.kodein.db.Options
import org.kodein.db.kv.KeyValueBatch
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.ReadMemory


internal class KeyValueBatchImpl(private val levelDB: LevelDB, private val writeBatch: LevelDB.WriteBatch) : KeyValueBatch {

    override fun put(key: ReadMemory, value: ReadMemory, vararg options: Options.BatchPut) { writeBatch.put(key, value) }

    override fun delete(key: ReadMemory, vararg options: Options.BatchDelete) { writeBatch.delete(key) }

    override fun clear() { writeBatch.clear() }

    override fun append(source: KeyValueBatch) { writeBatch.append((source as? KeyValueBatchImpl)?.writeBatch ?: error("Source is not a KLevelWriteBatchImpl")) }

    override fun write(vararg options: Options.BatchWrite) { levelDB.write(writeBatch, LevelDB.WriteOptions.from(options)) }

    override fun close() { writeBatch.close() }
}
