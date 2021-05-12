package org.kodein.db.test.utils

import org.kodein.memory.io.*
import org.kodein.memory.text.arrayFromHex
import org.kodein.memory.text.toHex
import org.kodein.memory.text.writeString
import kotlin.test.fail

fun int(v: Int) = Memory.array(4) { writeInt(v) }
fun hex(h: String) = Memory.arrayFromHex(h)

@OptIn(ExperimentalUnsignedTypes::class)
fun ushort(v: Int) = Memory.array(4) { writeUShort(v.toUShort()) }

private fun Writeable.writeValues(vararg values: Any) {
    for (value in values) {
        when (value) {
            is Number -> writeByte(value.toByte())
            is Char -> writeByte(value.toByte())
            is CharSequence -> writeString(value)
            is ReadMemory -> writeBytes(value)
            is ByteArray -> writeBytes(value)
            else -> throw IllegalArgumentException(value.toString())
        }
    }
}

fun array(vararg values: Any): ByteArray =
    Memory.array(16384) {
        writeValues(*values)
    } .getBytes()

fun native(vararg values: Any): Allocation =
    Allocation.native(16384) {
        writeValues(*values)
    }

@OptIn(ExperimentalUnsignedTypes::class)
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
            2 -> sb.append(b.toUByte().toUInt().toString(16).padStart(2, '0'))
        }
    }

    if (type == 1)
        sb.append("\"")
    return sb.toString()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.hex(): String = joinToString { it.toUByte().toUInt().toString(16).toUpperCase().padStart(2, '0') }

fun assertBytesEquals(expected: ByteArray, actual: ByteArray, description: Boolean = true, prefix: String = "") {
    if (!expected.contentEquals(actual)) {
        if (description)
            fail("${prefix}Bytes are not equal:\nExpected: ${expected.description()}\nActual:   ${actual.description()}")
        else
            fail("${prefix}Bytes are not equal:\nExpected: ${expected.hex()}\nActual:   ${actual.hex()}")    }
}

fun assertBytesEquals(expected: ByteArray, actual: ReadMemory, description: Boolean = true, prefix: String = "") =
        assertBytesEquals(expected, actual.getBytes(), description, prefix)

fun assertBytesEquals(expected: ReadMemory, actual: ReadMemory, description: Boolean = true) =
        assertBytesEquals(expected.getBytes(), actual.getBytes(), description)
