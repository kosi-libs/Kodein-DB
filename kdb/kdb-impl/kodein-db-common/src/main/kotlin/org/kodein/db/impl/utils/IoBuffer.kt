package org.kodein.db.impl.utils

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.discardExact
import org.kodein.db.Body
import org.kodein.db.Value


internal fun IoBuffer.writeFully(value: Body) = value.writeInto(this)

internal inline fun <T> IoBuffer.applyViewOf(view: IoBuffer, on: T, block: T.() -> Unit) {
    val startPos = readRemaining

    on.block()

    view.discardExact(startPos)
    view.reserveEndGap(view.writeRemaining)
}

internal inline fun IoBuffer.makeViewOf(block: IoBuffer.() -> Unit): IoBuffer = makeView().also { applyViewOf(it, this, block) }

internal fun IoBuffer.firstPositionOf(search: Byte, discard: Int = 0): Int {
    require(discard >= 0)

    val view = makeView()
    if (discard > 0)
        view.discardExact(discard)

    var i = discard
    while (view.canRead()) {
        if (view.readByte() == search)
            return i
        ++i
    }

    return -1
}

internal fun IoBuffer.applySubView(view: IoBuffer, startPos: Int, length: Int = -1) {
    val bufferSize = this.capacity - this.writeRemaining
    val after = if (length >= 0) bufferSize - this.startGap - startPos - length else 0

    require(after >= 0) { "Cannot make a sub view that ends after write position (contains data not yet written)" }

    if (after == 0) {
        view.reserveEndGap(this.writeRemaining)
    }
    else {
        view.resetForWrite(this.startGap + startPos + length)
    }
    view.resetForRead()
    view.discardExact(this.startGap + startPos)
}

internal fun IoBuffer.makeSubView(startPos: Int, length: Int = -1): IoBuffer = makeView().also { applySubView(it, startPos, length) }

internal fun IoBuffer.startsWith(other: IoBuffer): Boolean {
    if (this.readRemaining < other.readRemaining)
        return false

    val thisView = makeView()
    val otherView = other.makeView()

    while (otherView.canRead()) {
        if (thisView.readByte() != otherView.readByte())
            return false
    }

    return true
}

internal operator fun IoBuffer.compareTo(other: IoBuffer): Int {
    val thisView = makeView()
    val otherView = other.makeView()

    while (thisView.canRead() && otherView.canRead()) {
        val comparison = thisView.readByte() - otherView.readByte()
        if (comparison != 0)
            return comparison
    }

    return thisView.readRemaining - otherView.readRemaining
}
