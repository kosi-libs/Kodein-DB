package org.kodein.db.leveldb.jni

import org.kodein.memory.io.*

internal fun ReadMemory.directJvmNioKBuffer() = (internalBuffer() as? JvmNioKBuffer)?.takeIf { it.isDirect }

internal fun ReadMemory.array() = (internalBuffer() as? KBuffer)?.backingArray() ?: getBytes(0)

internal fun ReadMemory.arrayOffset() = (internalBuffer() as? KBuffer)?.takeIf { it.backingArray() != null } ?.absPosition ?: 0
