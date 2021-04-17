package org.kodein.db.leveldb.jni

import org.kodein.memory.io.*

internal fun ReadMemory.directByteBuffer() = (internalMemory() as? DirectByteBufferMemory)?.byteBuffer

internal fun ReadMemory.array() = (internalMemory() as? ByteArrayMemory)?.array ?: getBytes()

internal fun ReadMemory.arrayOffset() = (internalMemory() as? ByteArrayMemory)?.offset ?: 0
