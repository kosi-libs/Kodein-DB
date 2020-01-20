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

internal fun ReadBuffer.verifyObjectKey() {
    mark(this) {
        require(read() == Prefix.OBJECT) { "Bad key" }
        require(read() == NULL) { "Bad key" }
        require(firstIndexOf(NULL, 2) > 0) { "Bad key" }
    }
}

internal val objectEmptyPrefix: ByteArray = byteArrayOf(Prefix.OBJECT, NULL)

internal fun Writeable.putObjectKey(type: String, id: Value?, isOpen: Boolean = false) {
    put(Prefix.OBJECT)
    put(NULL)

    putAscii(type)
    put(NULL)

    if (id != null) {
        putBody(id)
        if (!isOpen)
            put(NULL)
    }
}

internal fun getObjectKeySize(type: String, id: Value?, isOpen: Boolean = false): Int {
    var size = (
            2                      // PREFIX_OBJECT + NULL
        +   type.length + 1)       // type + NULL

    if (id != null) {
        size += id.size    // id
        if (!isOpen)
            size += 1              // NULL
    }

    return size
}

internal fun Writeable.putRefKeyFromObjectKey(objectKey: ReadBuffer) {
    mark(objectKey) {
        put(Prefix.REFERENCE)
        objectKey.skip(1)
        putBytes(objectKey)
    }
}

internal fun getObjectKeyType(key: ReadBuffer): ReadBuffer {
    val typeEnd = key.firstIndexOf(NULL, key.position + 2)
    check(typeEnd != -1)
    return key.slice(2, typeEnd - 2)
}

internal fun getObjectKeyID(key: ReadBuffer): ReadBuffer {
    val typeEnd = key.firstIndexOf(NULL, key.position + 2)
    check(typeEnd != -1)
    return key.slice(typeEnd + 1, key.limit - typeEnd - 2)
}

internal fun getIndexKeyName(key: ReadBuffer): ReadBuffer {
    val typeEnd = key.firstIndexOf(NULL, key.position + 2)
    check(typeEnd != -1)

    val nameEnd = key.firstIndexOf(NULL, typeEnd + 1)
    check(nameEnd != -1)

    val nameSize = nameEnd - typeEnd - 1
    return key.slice(typeEnd + 1, nameSize)
}

private fun Writeable.putIndexKey(type: ReadBuffer, id: ReadBuffer, name: String, value: Value) {
    mark(type, id) {
        put(Prefix.INDEX)
        put(NULL)

        putBytes(type)
        put(NULL)

        putAscii(name)
        put(NULL)

        putBody(value)
        put(NULL)

        putBytes(id)
        put(NULL)
    }
}

internal fun Writeable.putIndexKey(objectKey: ReadBuffer, name: String, value: Value) {
    val type = getObjectKeyType(objectKey)
    val id = getObjectKeyID(objectKey)

    putIndexKey(type, id, name, value)
}

internal fun getIndexKeySize(objectKey: ReadBuffer, name: String, value: Value): Int {
    val type = getObjectKeyType(objectKey)
    val id = getObjectKeyID(objectKey)

    return (
            2                   // PREFIX_INDEX + NULL
        +   type.remaining + 1  // type + NULL
        +   name.length + 1     // name + NULL
        +   value.size + 1      // value + NULL
        +   id.remaining + 1    // id + NULL
    )
}

@Suppress("DuplicatedCode")
internal fun Writeable.putIndexKeyStart(type: String, name: String, value: Value?, isOpen: Boolean = false) {
    put(Prefix.INDEX)
    put(NULL)

    putAscii(type)
    put(NULL)

    putAscii(name)
    put(NULL)

    if (value != null) {
        putBody(value)
        if (!isOpen)
            put(NULL)
    }
}

internal fun getIndexKeyStartSize(type: String, name: String, value: Value?, isOpen: Boolean = false): Int {
    var size = (
            2                           // PREFIX_INDEX + NULL
        +   type.length + 1             // type + NULL
        +   name.length + 1             // name + NULL
    )

    if (value != null) {
        size += value.size         // value
        if (!isOpen)
            size += 1              // NULL
    }

    return size
}
