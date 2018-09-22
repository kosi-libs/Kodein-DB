package org.kodein.db.impl.utils

import org.kodein.db.leveldb.Bytes

internal inline fun Bytes.makeViewOf(block: Bytes.() -> Unit): Bytes = makeView().also { buffer.applyViewOf(it.buffer, this, block) }

internal fun Bytes.makeSubView(startPos: Int, length: Int = -1): Bytes = makeView().also { buffer.applySubView(it.buffer, startPos, length) }
