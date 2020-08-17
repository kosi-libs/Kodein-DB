package org.kodein.db.leveldb.native

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.plus
import kotlinx.cinterop.refTo
import org.kodein.memory.io.*

public fun ReadMemory.pointer(): CValuesRef<ByteVar> = when (val b = internalBuffer()) {
    is CPointerKBuffer -> (b.pointer + b.absPosition)!!
    is ByteArrayKBuffer -> { b.array.refTo(b.absPosition) }
    else -> { b.getBytes(0).refTo(0) }
}
