package org.kodein.db

import org.kodein.db.ascii.putAscii
import org.kodein.memory.io.*

interface Value : Body {

    val size: Int

    abstract class AbstractValue : Value {

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

    abstract class ZeroSpacedValues(private val _count: Int) : AbstractValue(), Value {

        final override fun writeInto(dst: Writeable) {
            for (i in 0 until _count) {
                if (i != 0)
                    dst.put(0.toByte())
                write(dst, i)
            }
        }

        override val size: Int get() {
            var size = _count - 1
            for (i in 0 until _count)
                size += size(i)
            return size
        }

        protected abstract fun write(dst: Writeable, pos: Int)
        protected abstract fun size(pos: Int): Int
    }

    companion object {

        val emptyValue: Value = object : Value {
            override val size = 0
            override fun writeInto(dst: Writeable) {}
        }

        fun of(vararg values: ByteArray): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putBytes(values[pos], 0, values[pos].size)
                }
                override fun size(pos: Int) = values[pos].size
                override fun toString() = values.joinToString()
            }
        }

        fun of(vararg values: ReadBuffer): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putBytes(values[pos].duplicate())
                }
                override fun size(pos: Int) = values[pos].remaining
                override fun toString() = values.joinToString()
            }
        }

        fun of(vararg values: Value): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    val value = values[pos]
                    value.writeInto(dst)
                }
                override fun size(pos: Int) = values[pos].size
                override fun toString() = values.joinToString()
            }
        }

        fun of(vararg values: Boolean): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.put((if (values[pos]) 1 else 0).toByte())
                }
                override fun size(pos: Int) = 1
                override fun toString() = values.joinToString()
            }
        }

        fun of(vararg values: Byte): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.put(values[pos])
                }
                override fun size(pos: Int) = 1
                override fun toString() = values.joinToString()
            }
        }

        fun of(vararg values: Short): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putShort(values[pos])
                }
                override fun size(pos: Int) = 2
                override fun toString() = values.joinToString()
            }
        }

        fun of(vararg values: Int): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putInt(values[pos])
                }
                override fun size(pos: Int) = 4
                override fun toString() = values.joinToString()
            }
        }

        fun of(vararg values: Long): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putLong(values[pos])
                }
                override fun size(pos: Int) = 8
                override fun toString() = values.joinToString()
            }
        }

        fun ofAscii(vararg values: Char): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.put(values[pos].toByte())
                }
                override fun size(pos: Int) = 1
                override fun toString() = values.joinToString()
            }
        }


        fun ofAscii(vararg values: CharSequence): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putAscii(values[pos])
                }
                override fun size(pos: Int) = values[pos].length
                override fun toString() = values.joinToString()
            }
        }

        fun ofAll(vararg values: Any): Value {
            if (values.isEmpty())
                return emptyValue

            val sized = Array(values.size) {
                when (val value = values[it]) {
                    is Value ->  value
                    is ByteArray -> of(value)
                    is ReadBuffer -> of(value)
                    is Allocation -> of(value)
                    is Boolean ->  of((if (value) 1 else 0).toByte())
                    is Byte ->  of(value)
                    is Char ->  ofAscii(value)
                    is Short ->  of(value)
                    is Int ->  of(value)
                    is Long ->  of(value)
                    is String ->  ofAscii(value)
                    else -> throw IllegalArgumentException("invalid value: $value")
                }
            }

            return of(*sized)
        }
    }

}
