package org.kodein.db.impl.model

import org.kodein.memory.io.*

private object Prefix {
    const val TYPE = 't'.toByte()
}

internal val nextTypeKey = KBuffer.wrap(byteArrayOf(Prefix.TYPE, 'I'.toByte()))

internal fun getTypeNameKeySize(typeName: ReadMemory) = 2 + typeName.size

public const val typeIdKeySize: Int = 2 + 4

internal fun Writeable.putTypeNameKey(typeName: ReadMemory) {
    putByte(Prefix.TYPE)
    putByte('n'.toByte())
    typeName.markBuffer { putBytes(it) }
}


internal fun Writeable.putTypeIdKey(typeId: Int) {
    putByte(Prefix.TYPE)
    putByte('i'.toByte())
    putInt(typeId)
}
