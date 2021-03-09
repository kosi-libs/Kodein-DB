package org.kodein.db.plugin.fts

import org.kodein.db.*
import org.kodein.db.leveldb.LevelDB
import org.kodein.db.model.ModelDB
import org.kodein.memory.io.*
import kotlin.reflect.KClass


public class FtsCursor<M : Any>(modelDB: ModelDB, private val modelType: KClass<M>, token: String, isOpen: Boolean, options: Array<out Options.Read>) : BaseCursor {

    private val snapshot = modelDB.newSnapshot(*options)
    private val cursor = modelDB.data.ldb.newCursor(LevelDB.ReadOptions.from(snapshot.data.snapshot, options))

    private val prefix = Allocation.native(ftsIndexKeyPrefixSize(token, isOpen)) { putFtsIndexKeyPrefix(token, isOpen) }

    private var cachedValid: Boolean? = null
    private var cachedKey: Key<M>? = null
    private var cachedModel: M? = null
    private var cachedToken: String? = null
    private var cachedField: String? = null

    private var lastKey: Allocation? = null

    init {
        seekToFirst()
    }

    private fun cacheReset() {
        cachedValid = null
        cachedKey = null
        cachedModel = null
        cachedToken = null
        cachedField = null
    }

    private fun isValidSeekKey(key: ReadMemory) = key.startsWith(prefix)

    override fun isValid(): Boolean =
        cachedValid ?: (cursor.isValid() && isValidSeekKey(cursor.transientKey())).also { cachedValid = it }

    override fun next() {
        cacheReset()
        cursor.next()
    }

    override fun prev() {
        cacheReset()
        cursor.prev()
    }

    override fun seekToFirst() {
        cacheReset()
        cursor.seekTo(prefix)
    }

    override fun seekToLast() {
        cacheReset()
        if (lastKey == null) {
            val prefix = prefix
            lastKey = Allocation.native(prefix.size + CORK.size) {
                putMemoryBytes(prefix)
                putBytes(CORK)
            }
        }
        cursor.seekTo(lastKey!!)
        while (cursor.isValid() && isValidSeekKey(cursor.transientKey()))
            cursor.next()
        if (cursor.isValid())
            cursor.prev()
        else
            cursor.seekToLast()
    }

    override fun seekTo(target: ReadMemory) {
        cacheReset()
        check(isValidSeekKey(target)) { "Not a valid seek key" }
        cursor.seekTo(target)
    }

    override fun transientSeekKey(): ReadMemory {
        check(isValid()) { "Cursor is not valid" }
        return cursor.transientKey().duplicate()
    }

    public fun key(): Key<M> {
        check(isValid()) { "Cursor is not valid" }
        return cachedKey ?: Key<M>(KBuffer.arrayCopy(cursor.transientKey().getFtsIndexKeyDocumentKey())).also { cachedKey = it }
    }

    public fun model(): M {
        check(isValid()) { "Cursor is not valid" }
        return cachedModel
            ?: snapshot.get(modelType, key())?.model?.also { cachedModel = it }
            ?: error("Inconsistent internal state: FTS entry points to invalid document entry")
    }

    public fun token(): String {
        check(isValid()) { "Cursor is not valid" }
        return cachedToken ?: cursor.transientKey().getFtsIndexKeyToken().also { cachedToken = it }
    }

    public fun field(): String {
        check(isValid()) { "Cursor is not valid" }
        return cachedField ?: cursor.transientKey().getFtsIndexKeyField().also { cachedField = it }
    }

    public fun transientMedatada(): ReadBuffer {
        check(isValid()) { "Cursor is not valid" }
        return cursor.transientValue()
    }

    override fun close() {
        lastKey?.close()
        prefix.close()
        cursor.close()
        snapshot.close()
    }

    public companion object {
        private val CORK = ByteArray(16) { 0xFF.toByte() }
    }
}
