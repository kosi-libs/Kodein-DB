package org.kodein.db.leveldb.native

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.plus
import kotlinx.cinterop.refTo
import org.kodein.memory.ByteArrayKBuffer
import org.kodein.memory.CPointerKBuffer
import org.kodein.memory.ReadBuffer
import org.kodein.memory.getBytes

fun ReadBuffer.pointer(): CValuesRef<ByteVar> = when (val b = internalBuffer()) {
    is CPointerKBuffer -> (b.pointer + b.position)!!
    is ByteArrayKBuffer -> { b.array.refTo(b.position) }
    else -> { b.getBytes(b.position).refTo(0) }
}
