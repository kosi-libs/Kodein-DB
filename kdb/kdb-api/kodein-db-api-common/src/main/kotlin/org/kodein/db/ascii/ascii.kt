package org.kodein.db.ascii

import kotlinx.io.core.IoBuffer

fun IoBuffer.writeAscii(str: CharSequence) {
    for (i in 0 until str.length)
        writeByte(str[i].toByte())
}
