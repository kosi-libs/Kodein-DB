package org.kodein.db.impl.utils

import kotlinx.io.core.discardExact
import org.kodein.db.leveldb.Bytes

internal inline fun Bytes.makeViewOf(block: Bytes.() -> Unit): Bytes = makeView().also { buffer.applyViewOf(it.buffer, this, block) }

internal fun Bytes.makeSubView(startPos: Int, length: Int = -1): Bytes = makeView().also { applySubView(it, startPos, length) }

internal fun Bytes.applySubView(view: Bytes, startPos: Int, length: Int = -1) {
    val bufferSize = buffer.capacity - buffer.writeRemaining
    val after = if (length >= 0) bufferSize - buffer.startGap - startPos - length else 0

    require(after >= 0) { "Cannot make a sub view that ends after write position (contains data not yet written)" }

    if (after == 0) {
        if (buffer.writeRemaining != 0) {
            view.buffer.reserveEndGap(buffer.writeRemaining)
        }
    }
    else {
        view.buffer.resetForWrite(buffer.startGap + startPos + length)
    }
    view.buffer.resetForRead()
    view.buffer.discardExact(buffer.startGap + startPos)
}


