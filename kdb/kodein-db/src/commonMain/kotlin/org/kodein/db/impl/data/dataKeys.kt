package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.ascii.putAscii
import org.kodein.db.impl.utils.firstIndexOf
import org.kodein.db.impl.utils.putBody
import org.kodein.memory.io.*

private object Prefix {
    const val DOCUMENT = 'o'.toByte()
    const val INDEX = 'i'.toByte()
    const val REFERENCE = 'r'.toByte()
}

internal const val NULL = 0.toByte()

internal fun ReadMemory.verifyDocumentKey() {
    require(get(0) == Prefix.DOCUMENT) { "Bad key" }
    require(get(1) == NULL) { "Bad key" }
    require(firstIndexOf(NULL, 2) > 0) { "Bad key" }
}

internal val emptyDocumentPrefix = byteArrayOf(Prefix.DOCUMENT, NULL)

internal fun Writeable.putDocumentKey(type: Int, id: Value?, isOpen: Boolean = false) {
    putByte(Prefix.DOCUMENT)
    putByte(NULL)

    putInt(type)

    if (id != null) {
        putBody(id)
        if (!isOpen)
            putByte(NULL)
    }
}

internal fun getDocumentKeySize(id: Value?, isOpen: Boolean = false): Int {
    var size = (
            2              // PREFIX_DOCUMENT + NULL
        +   4)             // type

    if (id != null) {
        size += id.size    // id
        if (!isOpen)
            size += 1      // NULL
    }

    return size
}

internal fun Writeable.putRefKeyFromDocumentKey(documentKey: ReadMemory) {
    putByte(Prefix.REFERENCE)
    documentKey.markBuffer {
        it.skip(1)
        putBytes(it)
    }
}

internal fun getDocumentKeyType(key: ReadMemory): Int {
    return key.getInt(2)
}

internal fun getDocumentKeyID(key: ReadMemory): ReadBuffer {
    return key.slice(6, key.size - 7)
}

internal fun getIndexKeyName(key: ReadMemory): ReadBuffer {
    val nameEnd = key.firstIndexOf(NULL, 6)
    check(nameEnd != -1)

    val nameSize = nameEnd - 6
    return key.slice(6, nameSize)
}

private fun Writeable.putIndexKey(type: Int, id: ReadMemory, name: String, value: Value) {
    putByte(Prefix.INDEX)
    putByte(NULL)

    putInt(type)

    putAscii(name)
    putByte(NULL)

    putBody(value)
    putByte(NULL)

    id.markBuffer { putBytes(it) }
    putByte(NULL)
}

internal fun Writeable.putIndexKey(documentKey: ReadMemory, name: String, value: Value) {
    val type = getDocumentKeyType(documentKey)
    val id = getDocumentKeyID(documentKey)

    putIndexKey(type, id, name, value)
}

internal fun getIndexKeySize(documentKey: ReadMemory, name: String, value: Value): Int {
    val id = getDocumentKeyID(documentKey)

    return (
            2                   // PREFIX_INDEX + NULL
        +   4                   // type
        +   name.length + 1     // name + NULL
        +   value.size + 1      // value + NULL
        +   id.size + 1         // id + NULL
    )
}

@Suppress("DuplicatedCode")
internal fun Writeable.putIndexKeyStart(type: Int, name: String, value: Value?, isOpen: Boolean = false) {
    putByte(Prefix.INDEX)
    putByte(NULL)

    putInt(type)

    putAscii(name)
    putByte(NULL)

    if (value != null) {
        putBody(value)
        if (!isOpen)
            putByte(NULL)
    }
}

internal fun getIndexKeyStartSize(name: String, value: Value?, isOpen: Boolean = false): Int {
    var size = (
            2                      // PREFIX_INDEX + NULL
        +   4                      // type
        +   name.length + 1        // name + NULL
    )

    if (value != null) {
        size += value.size         // value
        if (!isOpen)
            size += 1              // NULL
    }

    return size
}
