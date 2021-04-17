package org.kodein.db.impl.model

import org.kodein.memory.io.*

private object Prefix {
    const val TYPE = 't'.toByte()
}

internal val nextTypeKey = Memory.wrap(byteArrayOf(Prefix.TYPE, 'I'.toByte()))

internal fun getTypeNameKeySize(typeName: ReadMemory) = 2 + typeName.size

public const val typeIdKeySize: Int = 2 + 4

internal fun Writeable.putTypeNameKey(typeName: ReadMemory) {
    writeByte(Prefix.TYPE)
    writeByte('n'.toByte())
    writeBytes(typeName)
}


internal fun Writeable.putTypeIdKey(typeId: Int) {
    writeByte(Prefix.TYPE)
    writeByte('i'.toByte())
    writeInt(typeId)
}
