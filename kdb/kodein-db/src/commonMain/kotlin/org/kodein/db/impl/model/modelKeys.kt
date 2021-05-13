package org.kodein.db.impl.model

import org.kodein.memory.io.*

private object Prefix {
    const val TYPE = 't'.code.toByte()
}

internal val nextTypeKey = Memory.wrap(byteArrayOf(Prefix.TYPE, 'I'.code.toByte()))

internal fun getTypeNameKeySize(typeName: ReadMemory) = 2 + typeName.size

public const val typeIdKeySize: Int = 2 + 4

internal fun Writeable.putTypeNameKey(typeName: ReadMemory) {
    writeByte(Prefix.TYPE)
    writeByte('n'.code.toByte())
    writeBytes(typeName)
}


internal fun Writeable.putTypeIdKey(typeId: Int) {
    writeByte(Prefix.TYPE)
    writeByte('i'.code.toByte())
    writeInt(typeId)
}
