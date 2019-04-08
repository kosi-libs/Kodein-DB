package org.kodein.db.impl.utils

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.discardExact
import org.kodein.db.Body
import org.kodein.db.leveldb.Bytes


internal fun IoBuffer.writeFully(value: Body) = value.writeInto(this)

internal inline fun <T> IoBuffer.applyViewOf(view: IoBuffer, on: T, block: T.() -> Unit) {
    val startPos = startGap + readRemaining

    on.block()

    view.reserveEndGap(writeRemaining)
    view.resetForRead()

    view.discardExact(startPos)
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
