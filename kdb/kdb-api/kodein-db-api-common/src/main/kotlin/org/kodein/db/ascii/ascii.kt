package org.kodein.db.ascii

import kotlinx.io.core.IoBuffer

fun IoBuffer.writeAscii(str: CharSequence) {
    for (i in 0 until str.length)
        writeByte(str[i].toByte())
}

fun IoBuffer.readFullyAscii(): String {
    val size = readRemaining
    val array = CharArray(size)
    for (i in 0 until size)
        array[i] = readByte().toChar()
    return String(array)
}
