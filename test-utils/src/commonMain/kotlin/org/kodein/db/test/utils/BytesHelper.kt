package org.kodein.db.test.utils

import org.kodein.memory.io.*
import kotlin.test.fail

private fun KBuffer.putValues(vararg values: Any) {
    for (value in values) {
        when (value) {
            is Number -> put(value.toByte())
            is Char -> put(value.toByte())
            is String -> {
                for (i in 0 until value.length)
                    put(value[i].toByte())
            }
            else -> throw IllegalArgumentException(value.toString())
        }
    }
    flip()
}

fun byteArray(vararg values: Any): ByteArray {
    val buffer = KBuffer.array(16384)
    buffer.putValues(*values)
    return buffer.readBytes()
}

fun newBuffer(vararg values: Any): Allocation {
    val buffer = Allocation.native(16384)
    buffer.putValues(*values)
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

@UseExperimental(ExperimentalUnsignedTypes::class)
fun ByteArray.hex(): String = joinToString { it.toUByte().toUInt().toString(16).toUpperCase().padStart(2, '0') }

fun assertBytesEquals(expected: ByteArray, actual: ByteArray, description: Boolean = true) {
    if (!expected.contentEquals(actual)) {
        if (description)
            fail("Bytes are not equal:\nExpected: ${expected.description()}\nActual:   ${actual.description()}")
        else
            fail("Bytes are not equal:\nExpected: ${expected.hex()}\nActual:   ${actual.hex()}")    }
}

fun assertBytesEquals(expected: ByteArray, actual: ReadMemory, description: Boolean = true) =
        assertBytesEquals(expected, actual.duplicate().readBytes(), description)

fun assertBytesEquals(expected: ReadMemory, actual: ReadMemory, description: Boolean = true) =
        assertBytesEquals(expected.getBytes(0), actual.getBytes(0), description)
