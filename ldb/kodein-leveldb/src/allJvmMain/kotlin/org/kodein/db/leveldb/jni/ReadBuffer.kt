package org.kodein.db.leveldb.jni

import org.kodein.memory.*
import java.nio.ByteBuffer

internal fun ReadBuffer.directByteBuffer(): ByteBuffer? = (internalBuffer() as? JvmNioKBuffer)?.byteBuffer?.takeIf { it.isDirect }

internal fun ReadBuffer.array(): BackingArray = internalBuffer().let { it.backingArray() ?: BackingArray(it.getBytes(position)) }
