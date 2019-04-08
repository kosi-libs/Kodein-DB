package org.kodein.db.impl.data

import kotlinx.io.core.writeFully
import org.kodein.db.ascii.writeAscii
import org.kodein.db.Value
import org.kodein.db.impl.utils.firstPositionOf
import org.kodein.db.impl.utils.makeSubView
import org.kodein.db.impl.utils.writeFully
import org.kodein.db.leveldb.Allocation
import org.kodein.db.leveldb.Bytes

private object Prefix {
    const val OBJECT = 'o'.toByte()
    const val REFERENCE = 'r'.toByte()
    const val INDEX = 'i'.toByte()
}

private val NULL = 0.toByte()

internal val objectEmptyPrefix = Allocation.allocNativeBuffer(2).apply {
    buffer.writeByte(Prefix.OBJECT)
    buffer.writeByte(NULL)
}

internal fun Bytes.writeObjectKey(type: String, primaryKey: Value?, isOpen: Boolean = false) {
    buffer.writeByte(Prefix.OBJECT)
    buffer.writeByte(NULL)

    buffer.writeAscii(type)
    buffer.writeByte(NULL)

    if (primaryKey != null) {
        buffer.writeFully(primaryKey)
        if (!isOpen)
            buffer.writeByte(NULL)
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

internal fun Bytes.writeRefKeyFromObjectKey(objectKey: Bytes) {
    buffer.writeByte(Prefix.REFERENCE)
    val view = objectKey.makeView()
    view.buffer.discard(1)
    buffer.writeFully(view.buffer)
}

internal fun getObjectKeyType(key: Bytes): Bytes {
    val typeEnd = key.buffer.firstPositionOf(NULL, discard = 2)
    if (typeEnd == -1)
        throw IllegalStateException()
    return key.makeSubView(2, typeEnd - 2)
}

internal fun getObjectKeyID(key: Bytes): Bytes {
    val typeEnd = key.buffer.firstPositionOf(NULL, discard = 2)
    if (typeEnd == -1)
        throw IllegalStateException()
    return key.makeSubView(typeEnd + 1)
}

internal fun getIndexKeyName(key: Bytes): Bytes {
    val typeEnd = key.buffer.firstPositionOf(NULL, discard = 2)
    if (typeEnd == -1)
        throw IllegalStateException()

    val nameEnd = key.buffer.firstPositionOf(NULL, discard = typeEnd + 1)
    if (nameEnd == -1)
        throw IllegalStateException()

    val nameSize = nameEnd - typeEnd - 1
    return key.makeSubView(typeEnd + 1, nameSize)
}

private fun Bytes.writeIndexKey(type: Bytes, id: Bytes, name: String, value: Value) {
    buffer.writeByte(Prefix.INDEX)
    buffer.writeByte(NULL)

    buffer.writeFully(type.makeView().buffer)
    buffer.writeByte(NULL)

    buffer.writeAscii(name)
    buffer.writeByte(NULL)

    buffer.writeFully(value)
    buffer.writeByte(NULL)

    buffer.writeFully(id.makeView().buffer)
}

internal fun Bytes.writeIndexKey(objectKey: Bytes, name: String, value: Value) {
    val type = getObjectKeyType(objectKey)
    val id = getObjectKeyID(objectKey)

    writeIndexKey(type, id, name, value)
}

internal fun getIndexKeySize(objectKey: Bytes, name: String, value: Value): Int {
    val type = getObjectKeyType(objectKey)
    val id = getObjectKeyID(objectKey)

    return (
            2                              // PREFIX_INDEX + NULL
        +   type.buffer.readRemaining + 1  // type + NULL
        +   name.length + 1                // name + NULL
        +   value.size + 1                 // value + NULL
        +   id.buffer.readRemaining        // id
    )
}

internal fun Bytes.writeIndexKeyStart(type: String, name: String, value: Value?, isOpen: Boolean = false) {
    buffer.writeByte(Prefix.INDEX)
    buffer.writeByte(NULL)

    buffer.writeAscii(type)
    buffer.writeByte(NULL)

    buffer.writeAscii(name)
    buffer.writeByte(NULL)

    if (value != null) {
        buffer.writeFully(value)
        if (!isOpen)
            buffer.writeByte(NULL)
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
