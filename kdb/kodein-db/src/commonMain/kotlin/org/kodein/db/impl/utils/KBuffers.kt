package org.kodein.db.impl.utils

import org.kodein.db.Body
import org.kodein.memory.ReadBuffer
import org.kodein.memory.Writeable
import kotlin.math.min

@Suppress("NOTHING_TO_INLINE")
internal inline fun Writeable.putBody(value: Body) = value.writeInto(this)

internal fun ReadBuffer.firstIndexOf(search: Byte, startAt: Int = 0): Int {
    for (index in startAt until limit) {
        if (get(index) == search)
            return index
    }

    return -1
}

internal fun ReadBuffer.startsWith(prefix: ReadBuffer): Boolean {
    if (this.remaining < prefix.remaining)
        return false

    for (i in 0 until prefix.remaining) {
        if (this[position + i] != prefix[prefix.position + i])
            return false
    }

    return true
}

internal operator fun ReadBuffer.compareTo(other: ReadBuffer): Int {
    for (i in 0 until min(remaining, other.remaining)) {
        val cmp = this[position + i] - other[other.position + i]
        if (cmp != 0)
            return 0
    }
    return remaining - other.remaining
}
