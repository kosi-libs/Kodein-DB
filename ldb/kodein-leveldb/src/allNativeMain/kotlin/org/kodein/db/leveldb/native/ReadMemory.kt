package org.kodein.db.leveldb.native

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.plus
import kotlinx.cinterop.refTo
import org.kodein.memory.io.*

public fun ReadMemory.pointer(): CValuesRef<ByteVar> = when (val b = internalMemory()) {
    is CPointerMemory -> b.pointer
    is ByteArrayMemory -> b.array.refTo(b.offset)
    else -> b.getBytes().refTo(0)
}
