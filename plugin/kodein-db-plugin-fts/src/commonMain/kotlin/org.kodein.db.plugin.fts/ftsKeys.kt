package org.kodein.db.plugin.fts

import org.kodein.db.ExtensionPrefixByte
import org.kodein.memory.io.*
import org.kodein.memory.text.*
import org.kodein.memory.use


/*
    ftsIndexKey = "XftsT" 0 token 0 field 0 documentKey
    ftsRefKey   = "XftsR" 0 documentKey field

    ftsIndexKey -> position
    ftsRefKey -> (token 0)+
*/

private object Prefix {
    val INDEX = byteArrayOf(ExtensionPrefixByte, 'f'.toByte(), 't'.toByte(), 's'.toByte(), 'T'.toByte())
    val REF   = byteArrayOf(ExtensionPrefixByte, 'f'.toByte(), 't'.toByte(), 's'.toByte(), 'R'.toByte())
}

private const val NULL = 0.toByte()



//region Index Key
internal fun ftsIndexKeyPrefixSize(token: String, isOpen: Boolean): Int {
    var size = Prefix.INDEX.size + 1 // "XftsT" 0
    size += Charset.UTF8.sizeOf(token)  // token
    if (!isOpen) size += 1 // 0
    return size
}

internal fun ftsIndexKeySize(token: String, field: String, documentKey: ReadMemory): Int {
    var size = ftsIndexKeyPrefixSize(token, false) // "XftsT" 0 token 0
    size += field.length + 1 // field 0
    size += documentKey.size // documentKey
    return size
}

internal fun Writeable.putFtsIndexKeyPrefix(token: String, isOpen: Boolean) {
    putBytes(Prefix.INDEX)
    putByte(NULL)
    putString(token, Charset.UTF8)
    if (!isOpen) putByte(NULL)
}

internal fun Writeable.putFtsIndexKey(token: String, field: String, documentKey: ReadMemory) {
    putFtsIndexKeyPrefix(token, false)
    putString(field, Charset.ASCII)
    putByte(NULL)
    putMemoryBytes(documentKey)
}

internal fun ReadMemory.getFtsIndexKeyToken(): String =
    markBuffer(6) {
        it.readStringThenNull(Charset.UTF8)
    }

internal fun ReadMemory.getFtsIndexKeyField(): String {
    val tokenEnd = firstIndexOf(NULL, 6) + 1
    markBuffer(tokenEnd) {
        return it.readStringThenNull(Charset.ASCII)
    }
}

internal fun ReadMemory.getFtsIndexKeyDocumentKey(): ReadMemory {
    val tokenEnd = firstIndexOf(NULL, 6) + 1
    val fieldEnd = firstIndexOf(NULL, tokenEnd) + 1
    return slice(fieldEnd)
}

internal fun ReadMemory.getFtsIndexKeyDocumentType(): Int {
    val tokenEnd = firstIndexOf(NULL, 6) + 1
    val fieldEnd = firstIndexOf(NULL, tokenEnd) + 1
    return getInt(fieldEnd + 2)
}
//endregion


//region Ref Key
internal fun ftsRefKeyPrefixSize(documentKey: ReadMemory): Int {
    var size = Prefix.REF.size + 1 // "XftsR" 0
    size += documentKey.size // documentKey
    return size
}

internal fun ftsRefKeySize(documentKey: ReadMemory, field: String): Int {
    var size = ftsRefKeyPrefixSize(documentKey) // "XftsR" 0 documentKey
    size += Charset.ASCII.sizeOf(field)
    return size
}

internal fun Writeable.putFtsRefKeyPrefix(documentKey: ReadMemory) {
    putBytes(Prefix.REF)
    putByte(NULL)
    putMemoryBytes(documentKey)
}

internal fun Writeable.putFtsRefKey(documentKey: ReadMemory, field: String) {
    putFtsRefKeyPrefix(documentKey)
    putString(field, Charset.ASCII)
}

internal fun ReadMemory.getFtsRefKeyField(): String {
    val fieldStart = lastIndexOf(NULL) + 1
    markBuffer(fieldStart) {
        return it.readString(Charset.ASCII)
    }
}
//endregion


//region Ref Value
internal fun Writeable.putFtsRefValueToken(token: String) {
    putStringThenNull(token)
}

internal fun ReadMemory.getFtsRefValueTokens(): Sequence<String> {
    val buffer = duplicate()
    return sequence {
        while (buffer.valid()) {
            val token = buffer.readStringThenNull(Charset.UTF8)
            yield(token)
        }
    }
}
//endregion
