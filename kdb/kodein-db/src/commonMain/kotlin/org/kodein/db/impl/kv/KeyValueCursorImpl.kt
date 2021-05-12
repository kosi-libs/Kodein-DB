package org.kodein.db.impl.kv

import org.kodein.db.kv.KeyValueCursor
import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.io.ReadMemory

internal class KeyValueCursorImpl(private val cursor: LevelDB.Cursor) : KeyValueCursor {

    override fun transientKey(): ReadMemory = cursor.transientKey()

    override fun transientValue(): ReadMemory = cursor.transientValue()

    override fun isValid(): Boolean = cursor.isValid()

    override fun next() { cursor.next() }

    override fun prev() { cursor.prev() }

    override fun seekToFirst() { cursor.seekToFirst() }

    override fun seekToLast() { cursor.seekToLast() }

    override fun seekTo(target: ReadMemory) { cursor.seekTo(target) }

    override fun transientSeekKey(): ReadMemory = cursor.transientKey()

    override fun close() { cursor.close() }
}
