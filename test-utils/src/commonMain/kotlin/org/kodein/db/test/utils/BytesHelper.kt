package org.kodein.db.test.utils

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readBytes
import org.kodein.db.leveldb.Allocation
import org.kodein.db.leveldb.Bytes
import org.kodein.db.leveldb.LevelDB
import kotlin.test.fail

fun byteArray(vararg values: Any): ByteArray {
    val builder = BytePacketBuilder()
    for (value in values) {
        when (value) {
            is Number -> builder.writeByte(value.toByte())
            is Char -> builder.writeByte(value.toByte())
            is String -> {
                for (i in 0 until value.length)
                    builder.writeByte(value[i].toByte())
            }
            else -> throw IllegalArgumentException(value.toString())
        }
    }

    return builder.build().readBytes()
}

fun newBuffer(vararg values: Any): Allocation {
    val bytes = byteArray(*values)
    val buffer = Allocation.allocNativeBuffer(bytes.size)
    buffer.buffer.writeFully(bytes, 0, bytes.size)
    return buffer
}

fun ByteArray.description(): String {
    if (isEmpty())
        return "\"\""

    var type = 0 // 0 = start, 1 = chars, 2 = ints

    val sb = StringBuilder()
    for (b in this) {
        val newType = if (b in 32..126) 1 else 2
        if (type != newType && type == 1)
            sb.append("\"")
        if (type != 0 && type != newType)
            sb.append(", ")
        if (type != newType && newType == 1)
            sb.append("\"")
        type = newType
        when (type) {
            1 -> sb.append(b.toChar())
            2 -> sb.append("x" + b.toInt().toString(16))
        }
    }
    if (type == 1)
        sb.append("\"")
    return sb.toString()
}

fun assertBytesEquals(expected: ByteArray, actual: ByteArray) {
    if (!expected.contentEquals(actual))
        fail("Bytes are not equal: ${expected.description()} != ${actual.description()}")
}

fun assertBytesEquals(expected: ByteArray, buffer: IoBuffer) =
        assertBytesEquals(expected, buffer.makeView().readBytes())

fun assertBytesEquals(expected: ByteArray, actual: Bytes) =
        assertBytesEquals(expected, actual.buffer.makeView().readBytes())
