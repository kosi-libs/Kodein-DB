package org.kodein.db

import kotlinx.io.core.IoBuffer
import kotlinx.io.core.use
import kotlinx.io.core.writeFully
import org.kodein.db.ascii.writeAscii
import org.kodein.db.leveldb.Allocation

interface Value : Body {

    val size: Int

    abstract class AbstractValue : Value {

        private var _hashCode = 0

        override fun hashCode(): Int {
            if (_hashCode == 0) {
                Allocation.allocHeapBuffer(size).use {
                    writeInto(it.buffer)
                    _hashCode = it.hashCode()
                }
            }
            return _hashCode
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Value)
                return false

            if (size != other.size)
                return false

            Allocation.allocHeapBuffer(size).use { thisBuffer ->
                writeInto(thisBuffer.buffer)
                Allocation.allocHeapBuffer(size).use { otherBuffer ->
                    other.writeInto(otherBuffer.buffer)
                    return thisBuffer == otherBuffer
                }
            }
        }
    }

    abstract class ZeroSpacedValues(private val _count: Int) : AbstractValue(), Value {

        final override fun writeInto(dst: IoBuffer) {
            for (i in 0 until _count) {
                if (i != 0)
                    dst.writeByte(0.toByte())
                write(dst, i)
            }
        }

        override val size: Int get() {
            var size = _count - 1
            for (i in 0 until _count)
                size += size(i)
            return size
        }

        protected abstract fun write(dst: IoBuffer, pos: Int)
        protected abstract fun size(pos: Int): Int
    }

    companion object {

        val EmptyValue: Value = object : Value {
            override val size = 0
            override fun writeInto(dst: IoBuffer) {}
        }

        fun of(vararg values: ByteArray): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeFully(values[pos], 0, values[pos].size)
                }
                override fun size(pos: Int) = values[pos].size
            }
        }

        fun of(vararg values: Allocation): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeFully(values[pos].buffer.makeView())
                }
                override fun size(pos: Int) = values[pos].buffer.readRemaining
            }
        }

        fun of(vararg values: IoBuffer): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeFully(values[pos].makeView())
                }
                override fun size(pos: Int) = values[pos].readRemaining
            }
        }

        fun of(vararg values: Value): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    val value = values[pos]
                    value.writeInto(dst)
                }
                override fun size(pos: Int) = values[pos].size
            }
        }

        fun of(vararg values: Boolean): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeByte((if (values[pos]) 0 else 1).toByte())
                }
                override fun size(pos: Int) = 1
            }
        }

        fun of(vararg values: Byte): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeByte(values[pos])
                }
                override fun size(pos: Int) = 1
            }
        }

        fun of(vararg values: Short): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeShort(values[pos])
                }
                override fun size(pos: Int) = 2
            }
        }

        fun of(vararg values: Int): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeInt(values[pos])
                }

                override fun size(pos: Int) = 4
            }
        }

        fun of(vararg values: Long): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeLong(values[pos])
                }

                override fun size(pos: Int) = 8
            }
        }

        fun ofAscii(vararg values: Char): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeByte(values[pos].toByte())
                }
                override fun size(pos: Int) = 1
            }
        }


        fun ofAscii(vararg values: CharSequence): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: IoBuffer, pos: Int) {
                    dst.writeAscii(values[pos])
                }
                override fun size(pos: Int) = values[pos].length
            }
        }

        fun ofAll(vararg values: Any): Value {
            if (values.isEmpty())
                return EmptyValue

            val sized = Array<Value>(values.size) {
                when (val value = values[it]) {
                    is Value ->  value
                    is ByteArray -> of(*value)
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
