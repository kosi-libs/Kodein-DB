package org.kodein.db.impl.model

import org.kodein.db.impl.data.NULL
import org.kodein.memory.io.*

private object Prefix {
    const val TYPE = 't'.toByte()
}

internal val nextTypeKey = KBuffer.wrap(byteArrayOf(Prefix.TYPE, 255.toByte()))

internal fun getTypeKeySize(typeName: ReadMemory) = 2 + typeName.size

internal fun Writeable.putTypeKey(typeName: ReadMemory) {
    put(Prefix.TYPE)
    put(NULL)
    typeName.markBuffer { putBytes(it) }
}
