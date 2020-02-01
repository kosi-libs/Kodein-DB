package org.kodein.db.leveldb.jni

import org.kodein.memory.io.*
import java.nio.ByteBuffer

internal fun ReadMemory.directByteBuffer(): ByteBuffer? = (internalBuffer() as? JvmNioKBuffer)?.byteBuffer?.takeIf { it.isDirect }

internal fun ReadMemory.array(): BackingArray = internalBuffer().let { it.backingArray() ?: BackingArray(it.getBytes(0)) }
