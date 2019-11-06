package org.kodein.db.ascii

import org.kodein.memory.io.Readable
import org.kodein.memory.io.Writeable


fun Writeable.putAscii(str: CharSequence) {
    for (char in str)
        put(char.toByte())
}

fun Readable.readAscii(size: Int = remaining): String {
    val array = CharArray(size)
    for (i in 0 until size)
        array[i] = read().toChar()
    return String(array)
}
