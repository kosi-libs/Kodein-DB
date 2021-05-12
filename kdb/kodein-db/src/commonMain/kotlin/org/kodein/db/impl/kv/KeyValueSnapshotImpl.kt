package org.kodein.db.impl.kv

import org.kodein.db.Options
import org.kodein.db.kv.KeyValueCursor
import org.kodein.db.kv.KeyValueSnapshot
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.ReadMemory


internal class KeyValueSnapshotImpl(private val ldb: LevelDB, private val snapshot: LevelDB.Snapshot) : KeyValueSnapshot {

    override fun get(key: ReadMemory, vararg options: Options.Get): Allocation? = ldb.get(key, LevelDB.ReadOptions.from(options, snapshot))

    override fun newCursor(vararg options: Options.Find): KeyValueCursor = KeyValueCursorImpl(ldb.newCursor(LevelDB.ReadOptions.from(options, snapshot)))

    override fun close() { snapshot.close() }
}
