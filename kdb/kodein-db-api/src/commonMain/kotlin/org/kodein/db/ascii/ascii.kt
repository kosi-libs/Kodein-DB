package org.kodein.db.ascii

import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.Writeable


public fun Writeable.putAscii(str: CharSequence) {
    for (char in str)
        putByte(char.toByte())
}

public fun ReadMemory.getAscii(start: Int = 0, size: Int = limit - start): String {
    val array = CharArray(size)
    for (i in 0 until size)
        array[i] = get(start + i).toChar()
    return String(array)
}
