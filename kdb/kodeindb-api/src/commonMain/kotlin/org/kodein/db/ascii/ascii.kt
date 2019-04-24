package org.kodein.db.ascii

import org.kodein.memory.Readable
import org.kodein.memory.Writeable

fun Writeable.putAscii(str: CharSequence) {
    for (i in 0 until str.length)
        put(str[i].toByte())
}

fun Readable.readAscii(): String {
    val size = remaining
    val array = CharArray(size)
    for (i in 0 until size)
        array[i] = read().toChar()
    return String(array)
}
