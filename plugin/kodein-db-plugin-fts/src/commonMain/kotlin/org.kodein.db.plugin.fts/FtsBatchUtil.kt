package org.kodein.db.plugin.fts

import org.kodein.db.leveldb.LevelDB
import org.kodein.memory.Closeable
import org.kodein.memory.io.*
import org.kodein.memory.text.Charset
import org.kodein.memory.text.sizeOf
import org.kodein.memory.use


internal open class FtsBatchUtilKeys: Closeable {
    protected val indexKey = ExpandableBuffer.native(256)
    protected val refKey = ExpandableBuffer.native(256)

    fun deleteTokens(documentKey: ReadMemory, db: LevelDB, writeBatch: LevelDB.WriteBatch) {
        refKey.reset()
        refKey.requireCanWrite(ftsRefKeyPrefixSize(documentKey))
        refKey.putFtsRefKeyPrefix(documentKey)
        refKey.flip()
        db.newCursor().use { cursor ->
            cursor.seekTo(refKey)
            while (cursor.isValid() && cursor.transientKey().startsWith(refKey)) {
                val field = cursor.transientKey().getFtsRefKeyField()
                cursor.transientValue().getFtsRefValueTokens().forEach { token ->
                    indexKey.reset()
                    indexKey.requireCanWrite(ftsIndexKeySize(token, field, documentKey))
                    indexKey.putFtsIndexKey(token, field, documentKey)
                    indexKey.flip()
                    writeBatch.delete(indexKey)
                }
                writeBatch.delete(cursor.transientKey())
                cursor.next()
            }
        }
    }

    override fun close() {
        indexKey.close()
        refKey.close()
    }
}

internal class FtsBatchUtil: FtsBatchUtilKeys() {

    private val indexValue = Allocation.native(4)
    private val refValue = ExpandableBuffer.native(512)

    fun putTokens(texts: FtsTexts, documentKey: ReadMemory, writeBatch: LevelDB.WriteBatch) {

        texts.forEach { (field, tokens) ->
            refKey.reset()
            refKey.requireCanWrite(ftsRefKeySize(documentKey, field))
            refKey.putFtsRefKey(documentKey, field)
            refKey.flip()

            refValue.reset()
            refValue.requireCanWrite(tokens.keys.sumOf { Charset.UTF8.sizeOf(it) + 1 })

            tokens.forEach { (token, metadata) ->
                indexKey.reset()
                indexKey.requireCanWrite(ftsIndexKeySize(token, field, documentKey))
                indexKey.putFtsIndexKey(token, field, documentKey)
                indexKey.flip()

                indexValue.reset()
                indexValue.putReadableBytes(metadata)
                indexValue.flip()

                writeBatch.put(indexKey, indexValue)

                refValue.putFtsRefValueToken(token)
            }

            refValue.flip()
            writeBatch.put(refKey, refValue)
        }

        writeBatch.put(refKey, refValue)
    }

    override fun close() {
        indexValue.close()
        refValue.close()
        super.close()
    }
}

