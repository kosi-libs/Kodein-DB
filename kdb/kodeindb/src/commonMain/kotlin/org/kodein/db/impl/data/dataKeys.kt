package org.kodein.db.impl.data

import org.kodein.db.Value
import org.kodein.db.ascii.putAscii
import org.kodein.db.impl.utils.firstIndexOf
import org.kodein.db.impl.utils.putBody
import org.kodein.memory.*

private object Prefix {
    const val OBJECT = 'o'.toByte()
    const val REFERENCE = 'r'.toByte()
    const val INDEX = 'i'.toByte()
}

private val NULL = 0.toByte()

internal val objectEmptyPrefix: KBuffer = KBuffer.array(2).apply {
    put(Prefix.OBJECT)
    put(NULL)
    flip()
}

internal fun Writeable.putObjectKey(type: String, primaryKey: Value?, isOpen: Boolean = false) {
    put(Prefix.OBJECT)
    put(NULL)

    putAscii(type)
    put(NULL)

    if (primaryKey != null) {
        putBody(primaryKey)
        if (!isOpen)
            put(NULL)
    }
}

internal fun getObjectKeySize(type: String, primaryKey: Value?, isOpen: Boolean = false): Int {
    var size = (
            2                      // PREFIX_OBJECT + NULL
        +   type.length + 1)       // type + NULL

    if (primaryKey != null) {
        size += primaryKey.size    // primaryKey
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
    if (typeEnd == -1)
        throw IllegalStateException()
    return key.slice(2, typeEnd - 2)
}

internal fun getObjectKeyID(key: ReadBuffer): ReadBuffer {
    val typeEnd = key.firstIndexOf(NULL, key.position + 2)
    if (typeEnd == -1)
        throw IllegalStateException()
    return key.slice(typeEnd + 1)
}

internal fun getIndexKeyName(key: ReadBuffer): ReadBuffer {
    val typeEnd = key.firstIndexOf(NULL, key.position + 2)
    if (typeEnd == -1)
        throw IllegalStateException()

    val nameEnd = key.firstIndexOf(NULL, typeEnd + 1)
    if (nameEnd == -1)
        throw IllegalStateException()

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
        +   id.remaining        // id
    )
}

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
