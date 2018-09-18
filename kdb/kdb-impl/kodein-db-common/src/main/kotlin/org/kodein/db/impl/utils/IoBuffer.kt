package org.kodein.db.impl.utils

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.discardExact
import org.kodein.db.Value


fun IoBuffer.writeFully(value: Value) = value.writeInto(this)

internal inline fun IoBuffer.makeViewOf(block: IoBuffer.() -> Unit) : IoBuffer {
    val startPos = readRemaining

    block()

    val view = makeView()
    view.discardExact(startPos)
    view.reserveEndGap(view.writeRemaining)

    return view
}

fun IoBuffer.firstPositionOf(search: Byte, past: Int = 0): Int {
    val view = makeView()
    if (past > 0)
        view.discardExact(past)

    var i = 0
    while (view.canRead()) {
        if (view.readByte() == search)
            return i
        ++i
    }

    return -1
}

fun IoBuffer.makeSubView(startPos: Int, length: Int = -1): IoBuffer {
    val bufferSize = this.capacity - this.writeRemaining
    val after = if (length >= 0) bufferSize - this.startGap - startPos - length else 0

    require(after >= 0) { "Cannot make a sub view that ends after write position (contains data not yet written)" }

    val view = this.makeView()
    if (after == 0) {
        view.reserveEndGap(this.writeRemaining)
    }
    else {
        view.resetForWrite()
        view.reserveEndGap(this.writeRemaining + after)
    }
    view.resetForRead()
    view.discardExact(this.startGap + startPos)

    return view
}
