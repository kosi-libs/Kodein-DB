package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.writeBody
import org.kodein.memory.io.*
import org.kodein.memory.text.Charset
import org.kodein.memory.text.writeString


/*
    documentKey = 'o' 0 type id 0
    indexKey = 'i' 0 type name 0 value 0 id 0
    refKey = 'r' 0 type id 0

    documentKey -> document
    indexKey -> documentKey
    refKey -> (indexKey.size indexKey)+
*/

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

internal fun Writeable.writeDocumentKey(type: Int, id: Value?, isOpen: Boolean = false) {
    writeByte(Prefix.DOCUMENT)
    writeByte(NULL)

    writeInt(type)

    if (id != null) {
        writeBody(id)
        if (!isOpen) writeByte(NULL)
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

internal fun Writeable.writeRefKeyFromDocumentKey(documentKey: ReadMemory) {
    writeByte(Prefix.REFERENCE)
    writeBytes(documentKey.sliceAt(1))
}

internal fun getDocumentKeyType(key: ReadMemory): Int {
    return key.getInt(2)
}

internal fun getDocumentKeyID(key: ReadMemory): ReadMemory {
    return key.slice(6, key.size - 7)
}

internal fun getIndexKeyName(key: ReadMemory): ReadMemory {
    val nameEnd = key.firstIndexOf(NULL, 6)
    check(nameEnd != -1)

    val nameSize = nameEnd - 6
    return key.slice(6, nameSize)
}

private fun Writeable.writeIndexKey(type: Int, id: ReadMemory, name: String, value: Value) {
    writeByte(Prefix.INDEX)
    writeByte(NULL)

    writeInt(type)

    writeString(name, Charset.UTF8)
    writeByte(NULL)

    writeBody(value)
    writeByte(NULL)

    writeBytes(id)
    writeByte(NULL)
}

internal fun Writeable.writeIndexKey(documentKey: ReadMemory, name: String, value: Value) {
    val type = getDocumentKeyType(documentKey)
    val id = getDocumentKeyID(documentKey)

    writeIndexKey(type, id, name, value)
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
internal fun Writeable.writeIndexKeyStart(type: Int, name: String, value: Value?, isOpen: Boolean = false) {
    writeByte(Prefix.INDEX)
    writeByte(NULL)

    writeInt(type)

    writeString(name, Charset.UTF8)
    writeByte(NULL)

    if (value != null) {
        writeBody(value)
        if (!isOpen)
            writeByte(NULL)
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
