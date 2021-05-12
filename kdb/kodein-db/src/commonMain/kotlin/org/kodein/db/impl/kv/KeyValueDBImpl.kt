package org.kodein.db.impl.kv

import org.kodein.db.Options
import org.kodein.db.kv.*
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.ReadMemory


internal class KeyValueDBImpl(override val ldb: LevelDB) : KeyValueDB, Closeable by ldb {

    override val path: String get() = ldb.path

    override fun put(key: ReadMemory, value: ReadMemory, vararg options: Options.DirectPut) { ldb.put(key, value, LevelDB.WriteOptions.from(options)) }

    override fun delete(key: ReadMemory, vararg options: Options.DirectDelete) { ldb.delete(key, LevelDB.WriteOptions.from(options)) }

    override fun newSnapshot(vararg options: Options.NewSnapshot): KeyValueSnapshot = KeyValueSnapshotImpl(ldb, ldb.newSnapshot())

    override fun newBatch(vararg options: Options.NewBatch): KeyValueBatch = KeyValueBatchImpl(ldb, ldb.newWriteBatch())

    override fun get(key: ReadMemory, vararg options: Options.Get): Allocation? = ldb.get(key, LevelDB.ReadOptions.from(options))

    override fun newCursor(vararg options: Options.Find): KeyValueCursor = KeyValueCursorImpl(ldb.newCursor(LevelDB.ReadOptions.from(options)))
}
