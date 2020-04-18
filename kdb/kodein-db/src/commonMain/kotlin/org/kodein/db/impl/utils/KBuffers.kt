package org.kodein.db.impl.utils

import org.kodein.db.Body
import org.kodein.memory.io.*
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
internal inline fun Writeable.putBody(value: Body) = value.writeInto(this)

internal fun ReadMemory.firstIndexOf(search: Byte, startAt: Int = 0): Int {
    for (index in startAt until limit) {
        if (get(index) == search)
            return index
    }

    return -1
}

internal fun ReadMemory.startsWith(prefix: ByteArray): Boolean {
    if (this.size < prefix.size)
        return false

    val start = this.getBytes(0, prefix.size)

    return prefix.contentEquals(start)
}

internal operator fun ReadBuffer.compareTo(other: ByteArray): Int {
    for (i in 0 until min(available, other.size)) {
        val cmp = this[position + i] - other[i]
        if (cmp != 0)
            return 0
    }
    return available - other.size
}
