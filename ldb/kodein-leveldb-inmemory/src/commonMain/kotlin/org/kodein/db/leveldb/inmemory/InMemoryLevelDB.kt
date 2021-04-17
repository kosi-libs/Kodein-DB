package org.kodein.db.leveldb.inmemory

import org.kodein.db.leveldb.LevelDB
import org.kodein.db.leveldb.LevelDBFactory
import org.kodein.db.leveldb.PlatformCloseable
import org.kodein.memory.io.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

public class InMemoryLevelDB(private val data: MutableMap<ReadMemory, ByteArray>, options: LevelDB.Options, private val onClose: () -> Unit = {}) : PlatformCloseable("DB", null, options), LevelDB {

    private val dbHandler = Handler()

    override val path: String get() = "[IN-MEMORY]"

    override fun put(key: ReadMemory, value: ReadMemory, options: LevelDB.WriteOptions) {
        checkIsOpen()
        data[Memory.arrayCopy(key)] = value.getBytes()
    }

    private class NativeBytes constructor(override val memory: Memory, handler: Handler, options: LevelDB.Options)
        : PlatformCloseable("Value", handler, options), Allocation, Memory by memory {
        override fun platformClose() {}
        override fun toString(): String = memory.toString()
    }

    override fun get(key: ReadMemory, options: LevelDB.ReadOptions): Allocation? {
        checkIsOpen()
        val readData = options.snapshot?.let {
            require(it is Snapshot)
            it.data
        } ?: data
        return readData[key]?.let { NativeBytes(Memory.wrap(it), dbHandler, this.options) }
    }

    override fun delete(key: ReadMemory, options: LevelDB.WriteOptions) {
        checkIsOpen()
        data.remove(key)
    }

    override fun newWriteBatch(): LevelDB.WriteBatch {
        checkIsOpen()
        return WriteBatch(dbHandler, this.options)
    }

    override fun write(batch: LevelDB.WriteBatch, options: LevelDB.WriteOptions) {
        checkIsOpen()
        require(batch is WriteBatch)
        batch.ops.forEach { it(data) }
    }

    override fun newCursor(options: LevelDB.ReadOptions): LevelDB.Cursor {
        checkIsOpen()
        val readData = options.snapshot?.let {
            require(it is Snapshot)
            it.data
        } ?: HashMap(data)
        return Cursor(readData, dbHandler, this.options)
    }

    override fun newSnapshot(): LevelDB.Snapshot {
        checkIsOpen()
        return Snapshot(HashMap(data), dbHandler, this.options)
    }

    override fun beforeClose() { dbHandler.close() }

    override fun platformClose() { onClose() }

    private class WriteBatch(handler: Handler, options: LevelDB.Options) : PlatformCloseable("WriteBatch", handler, options), LevelDB.WriteBatch {
        val ops = ArrayList<(MutableMap<ReadMemory, ByteArray>) -> Unit>()

        override fun put(key: ReadMemory, value: ReadMemory) {
            checkIsOpen()
            val keyCopy = Memory.arrayCopy(key)
            val valueBytes = value.getBytes()
            ops.add { it[keyCopy] = valueBytes }
        }

        override fun delete(key: ReadMemory) {
            checkIsOpen()
            val keyCopy = Memory.arrayCopy(key)
            ops.add { it.remove(keyCopy) }
        }

        override fun clear() {
            checkIsOpen()
            ops.clear()
        }

        override fun append(source: LevelDB.WriteBatch) {
            checkIsOpen()
            require(source is WriteBatch)
            ops.addAll(source.ops)
        }

        override fun platformClose() {}
    }

    private class Cursor(val data: Map<ReadMemory, ByteArray>, handler: Handler, options: LevelDB.Options) : PlatformCloseable("Cursor", handler, options), LevelDB.Cursor {
        val keys = data.keys.sortedWith(ReadMemory::compareTo)
        var pos = -1

        override fun isValid(): Boolean = pos in keys.indices

        override fun seekToFirst() {
            checkIsOpen()
            pos = 0
        }

        override fun seekToLast() {
            checkIsOpen()
            pos = keys.lastIndex
        }

        override fun next() {
            checkIsOpen()
            pos += 1
        }

        override fun prev() {
            checkIsOpen()
            pos -= 1
        }

        override fun seekTo(target: ReadMemory) {
            checkIsOpen()
            val targetBytes = target.getBytes()
            pos = keys.indexOfFirst { it >= targetBytes }
        }

        override fun transientKey(): ReadMemory {
            checkIsOpen()
            return keys[pos]
        }

        override fun transientValue(): ReadMemory {
            checkIsOpen()
            return Memory.wrap(data[keys[pos]]!!)
        }

        override fun platformClose() {}
    }

    private class Snapshot(val data: Map<ReadMemory, ByteArray>, handler: Handler, options: LevelDB.Options) : PlatformCloseable("Snapshot", handler, options), LevelDB.Snapshot {
        override fun platformClose() {}
    }
}
