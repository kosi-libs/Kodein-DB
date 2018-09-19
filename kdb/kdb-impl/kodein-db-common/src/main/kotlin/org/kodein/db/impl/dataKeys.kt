package org.kodein.db.impl

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.writeFully
import org.kodein.db.ascii.writeAscii
import org.kodein.db.Value
import org.kodein.db.impl.utils.firstPositionOf
import org.kodein.db.impl.utils.makeSubView
import org.kodein.db.impl.utils.writeFully

private object Prefix {
    const val OBJECT = 'o'.toByte()
    const val REFERENCE = 'r'.toByte()
    const val INDEX = 'i'.toByte()
}

private val NULL = 0.toByte()

internal fun IoBuffer.writeObjectKey(type: String, primaryKey: Value?, isOpen: Boolean = false) {
    writeByte(Prefix.OBJECT)
    writeByte(NULL)

    writeAscii(type)
    writeByte(NULL)

    if (primaryKey != null) {
        writeFully(primaryKey)
        if (!isOpen)
            writeByte(NULL)
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

internal fun IoBuffer.writeRefKeyFromObjectKey(objectKey: IoBuffer) {
    writeByte(Prefix.REFERENCE)
    val view = objectKey.makeView()
    view.discard(1)
    writeFully(view)
}

internal fun getObjectKeyType(key: IoBuffer): IoBuffer {
    val typeEnd = key.firstPositionOf(NULL, discard = 2)
    if (typeEnd == -1)
        throw IllegalStateException()
    return key.makeSubView(2, typeEnd - 2)
}

internal fun getObjectKeyID(key: IoBuffer): IoBuffer {
    val typeEnd = key.firstPositionOf(NULL, discard = 2)
    if (typeEnd == -1)
        throw IllegalStateException()
    return key.makeSubView(typeEnd + 1)
}

internal fun getIndexKeyName(key: IoBuffer): IoBuffer {
    val typeEnd = key.firstPositionOf(NULL, discard = 2)
    if (typeEnd == -1)
        throw IllegalStateException()

    val nameEnd = key.firstPositionOf(NULL, discard = typeEnd + 1)
    if (nameEnd == -1)
        throw IllegalStateException()

    val nameSize = nameEnd - typeEnd - 1
    return key.makeSubView(typeEnd + 1, nameSize)
}

private fun IoBuffer.writeIndexKey(type: IoBuffer, id: IoBuffer, name: String, value: Value) {
    writeByte(Prefix.INDEX)
    writeByte(NULL)

    writeFully(type.makeView())
    writeByte(NULL)

    writeAscii(name)
    writeByte(NULL)

    writeFully(value)
    writeByte(NULL)

    writeFully(id.makeView())
}

internal fun IoBuffer.writeIndexKeyFromObjectKey(objectKey: IoBuffer, name: String, value: Value) {
    val type = getObjectKeyType(objectKey)
    val id = getObjectKeyID(objectKey)

    writeIndexKey(type, id, name, value)
}

internal fun getIndexKeySizeFromObjectKey(objectKey: IoBuffer, name: String, value: Value): Int {
    val type = getObjectKeyType(objectKey)
    val id = getObjectKeyID(objectKey)

    return (
            2                           // PREFIX_INDEX + NULL
        +   type.readRemaining + 1      // type + NULL
        +   name.length + 1             // name + NULL
        +   value.size + 1              // value + NULL
        +   id.readRemaining            // id
    )
}

internal fun IoBuffer.writeIndexKeyStart(type: String, name: String, value: Value?, isOpen: Boolean = false) {
    writeByte(Prefix.INDEX)
    writeByte(NULL)

    writeAscii(type)
    writeByte(NULL)

    writeAscii(name)
    writeByte(NULL)

    if (value != null) {
        writeFully(value)
        if (!isOpen)
            writeByte(NULL)
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
