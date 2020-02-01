package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.ascii.putAscii
import org.kodein.db.impl.utils.firstIndexOf
import org.kodein.db.impl.utils.putBody
import org.kodein.memory.io.*

private object Prefix {
    const val OBJECT = 'o'.toByte()
    const val REFERENCE = 'r'.toByte()
    const val INDEX = 'i'.toByte()
}

private const val NULL = 0.toByte()

internal fun ReadMemory.verifyObjectKey() {
    require(get(0) == Prefix.OBJECT) { "Bad key" }
    require(get(1) == NULL) { "Bad key" }
    require(firstIndexOf(NULL, 2) > 0) { "Bad key" }
}

internal val objectEmptyPrefix: ByteArray = byteArrayOf(Prefix.OBJECT, NULL)

internal fun Writeable.putObjectKey(type: ReadMemory, id: Value?, isOpen: Boolean = false) {
    put(Prefix.OBJECT)
    put(NULL)

    putBytes(type.duplicate())
    put(NULL)

    if (id != null) {
        putBody(id)
        if (!isOpen)
            put(NULL)
    }
}

internal fun getObjectKeySize(typeSize: Int, id: Value?, isOpen: Boolean = false): Int {
    var size = (
            2                      // PREFIX_OBJECT + NULL
        +   typeSize + 1)       // type + NULL

    if (id != null) {
        size += id.size    // id
        if (!isOpen)
            size += 1              // NULL
    }

    return size
}

internal fun Writeable.putRefKeyFromObjectKey(objectKey: ReadMemory) {
    objectKey.markBuffer {
        put(Prefix.REFERENCE)
        it.skip(1)
        putBytes(it)
    }
}

internal fun getObjectKeyType(key: ReadMemory): ReadBuffer {
    val typeEnd = key.firstIndexOf(NULL, 2)
    check(typeEnd != -1)
    return key.slice(2, typeEnd - 2)
}

internal fun getObjectKeyID(key: ReadMemory): ReadBuffer {
    val typeEnd = key.firstIndexOf(NULL, 2)
    check(typeEnd != -1)
    return key.slice(typeEnd + 1, key.size - typeEnd - 2)
}

internal fun getIndexKeyName(key: ReadMemory): ReadBuffer {
    val typeEnd = key.firstIndexOf(NULL, 2)
    check(typeEnd != -1)

    val nameEnd = key.firstIndexOf(NULL, typeEnd + 1)
    check(nameEnd != -1)

    val nameSize = nameEnd - typeEnd - 1
    return key.slice(typeEnd + 1, nameSize)
}

private fun Writeable.putIndexKey(type: ReadMemory, id: ReadMemory, name: String, value: Value) {
    put(Prefix.INDEX)
    put(NULL)

    type.markBuffer { putBytes(it) }
    put(NULL)

    putAscii(name)
    put(NULL)

    putBody(value)
    put(NULL)

    id.markBuffer { putBytes(it) }
    put(NULL)
}

internal fun Writeable.putIndexKey(objectKey: ReadMemory, name: String, value: Value) {
    val type = getObjectKeyType(objectKey)
    val id = getObjectKeyID(objectKey)

    putIndexKey(type, id, name, value)
}

internal fun getIndexKeySize(objectKey: ReadMemory, name: String, value: Value): Int {
    val type = getObjectKeyType(objectKey)
    val id = getObjectKeyID(objectKey)

    return (
            2                   // PREFIX_INDEX + NULL
        +   type.size + 1  // type + NULL
        +   name.length + 1     // name + NULL
        +   value.size + 1      // value + NULL
        +   id.size + 1    // id + NULL
    )
}

@Suppress("DuplicatedCode")
internal fun Writeable.putIndexKeyStart(type: ReadMemory, name: String, value: Value?, isOpen: Boolean = false) {
    put(Prefix.INDEX)
    put(NULL)

    type.markBuffer { putBytes(it) }
    put(NULL)

    putAscii(name)
    put(NULL)

    if (value != null) {
        putBody(value)
        if (!isOpen)
            put(NULL)
    }
}

internal fun getIndexKeyStartSize(type: ReadMemory, name: String, value: Value?, isOpen: Boolean = false): Int {
    var size = (
            2                           // PREFIX_INDEX + NULL
        +   type.size + 1               // type + NULL
        +   name.length + 1             // name + NULL
    )

    if (value != null) {
        size += value.size         // value
        if (!isOpen)
            size += 1              // NULL
    }

    return size
}
