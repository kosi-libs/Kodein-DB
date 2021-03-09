package org.kodein.db

import org.kodein.memory.io.*
import org.kodein.memory.text.Charset
import org.kodein.memory.text.putString
import org.kodein.memory.text.sizeOf
import org.kodein.memory.text.toHexString

public interface Value : Body {

    public val size: Int

    public abstract class AbstractValue : Value {

        private var _hashCode = 0

        override fun hashCode(): Int {
            if (_hashCode == 0) {
                val buffer = KBuffer.array(size)
                writeInto(buffer)
                buffer.flip()
                _hashCode = buffer.hashCode()
            }
            return _hashCode
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Value)
                return false

            if (size != other.size)
                return false

            val thisBuffer = KBuffer.array(size)
            writeInto(thisBuffer)
            thisBuffer. flip()

            val otherBuffer = KBuffer.array(other.size)
            other.writeInto(otherBuffer)
            otherBuffer.flip()

            return thisBuffer == otherBuffer
        }
    }

    public class ZeroSpacedValues(private val values: Array<out Value>) : AbstractValue() {

        override fun writeInto(dst: Writeable) {
            for (i in values.indices) {
                if (i != 0)
                    dst.putByte(0.toByte())
                values[i].writeInto(dst)
            }
        }

        override val size: Int get() {
            var size = values.size - 1
            for (i in values.indices)
                size += values[i].size
            return size
        }

        override fun toString(): String = values.joinToString()
    }

    public companion object {

        public val emptyValue: Value = object : Value {
            override val size = 0
            override fun writeInto(dst: Writeable) {}
        }

        public fun of(first: Value, second: Value, vararg other: Value): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: Value): Value = ZeroSpacedValues(values)

        public fun of(value: ByteArray): Value =
            object : Value.AbstractValue() {
                override fun writeInto(dst: Writeable) { dst.putBytes(value) }
                override val size get() = value.size
                override fun toString() = value.toHexString()
            }
        public fun of(first: ByteArray, second: ByteArray, vararg other: ByteArray): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: ByteArray): Value = ZeroSpacedValues(Array(values.size) { of(values[it]) })

        public fun of(value: ReadMemory): Value =
            object : Value.AbstractValue() {
                override fun writeInto(dst: Writeable) { dst.putMemoryBytes(value) }
                override val size get() = value.size
                override fun toString() = value.getBytes(0).toHexString()
            }
        public fun of(first: ReadMemory, second: ReadMemory, vararg other: ReadMemory): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: ReadMemory): Value = ZeroSpacedValues(Array(values.size) { of(values[it]) })

        public fun of(value: Boolean): Value =
            object : Value.AbstractValue() {
                override fun writeInto(dst: Writeable) { dst.putByte((if (value) 1 else 0).toByte()) }
                override val size get() = 1
                override fun toString() = if (value) "true" else "false"
            }
        public fun of(first: Boolean, second: Boolean, vararg other: Boolean): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: Boolean): Value = ZeroSpacedValues(Array(values.size) { of(values[it]) })

        public fun of(value: Byte): Value =
            object : Value.AbstractValue() {
                override fun writeInto(dst: Writeable) { dst.putByte(value) }
                override val size get() = 1
                override fun toString() = "0x${value.toString(radix = 16).padStart(2, '0')}"
            }
        public fun of(first: Byte, second: Byte, vararg other: Byte): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: Byte): Value = ZeroSpacedValues(Array(values.size) { of(values[it]) })

        public fun of(value: Short): Value =
            object : Value.AbstractValue() {
                override fun writeInto(dst: Writeable) { dst.putShort(value) }
                override val size get() = 2
                override fun toString() = value.toString()
            }
        public fun of(first: Short, second: Short, vararg other: Short): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: Short): Value = ZeroSpacedValues(Array(values.size) { of(values[it]) })

        public fun of(value: Int): Value =
            object : Value.AbstractValue() {
                override fun writeInto(dst: Writeable) { dst.putInt(value) }
                override val size get() = 4
                override fun toString() = value.toString()
            }
        public fun of(first: Int, second: Int, vararg other: Int): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: Int): Value = ZeroSpacedValues(Array(values.size) { of(values[it]) })

        public fun of(value: Long): Value =
            object : Value.AbstractValue() {
                override fun writeInto(dst: Writeable) { dst.putLong(value) }
                override val size get() = 8
                override fun toString() = value.toString()
            }
        public fun of(first: Long, second: Long, vararg other: Long): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: Long): Value = ZeroSpacedValues(Array(values.size) { of(values[it]) })

        public fun of(value: Char): Value =
            object : Value.AbstractValue() {
                override fun writeInto(dst: Writeable) { Charset.UTF8.encode(value, dst) }
                override val size get() = Charset.UTF8.sizeOf(value)
                override fun toString() = "\'$value\'"
            }
        public fun of(first: Char, second: Char, vararg other: Char): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: Char): Value = ZeroSpacedValues(Array(values.size) { of(values[it]) })

        public fun of(value: CharSequence): Value =
            object : Value.AbstractValue() {
                override fun writeInto(dst: Writeable) { dst.putString(value, Charset.UTF8) }
                override val size get() = Charset.UTF8.sizeOf(value)
                override fun toString() = "\"$value\""
            }
        public fun of(first: CharSequence, second: CharSequence, vararg other: CharSequence): Value = ofAll(first, second, *other)
        public fun ofAll(vararg values: CharSequence): Value = ZeroSpacedValues(Array(values.size) { of(values[it]) })

        @Deprecated("Use of which doesn't restrict to ASCII anymore", replaceWith = ReplaceWith("this.of(*values)"), level = DeprecationLevel.ERROR)
        public fun ofAscii(vararg values: Char): Value = ofAll(*values)

        @Deprecated("Use of which doesn't restrict to ASCII anymore", replaceWith = ReplaceWith("this.of(*values)"), level = DeprecationLevel.ERROR)
        public fun ofAscii(vararg values: CharSequence): Value = ofAll(*values)
    }

}

public fun Value.toArrayBuffer(): ByteArrayKBuffer = KBuffer.array(size).also { writeInto(it) }
