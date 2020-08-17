package org.kodein.db

import org.kodein.db.ascii.putAscii
import org.kodein.memory.io.*
import org.kodein.memory.util.UUID
import org.kodein.memory.util.putUUID

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

    public abstract class ZeroSpacedValues(private val _count: Int) : AbstractValue(), Value {

        final override fun writeInto(dst: Writeable) {
            for (i in 0 until _count) {
                if (i != 0)
                    dst.putByte(0.toByte())
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

    public companion object {

        public val emptyValue: Value = object : Value {
            override val size = 0
            override fun writeInto(dst: Writeable) {}
        }

        public fun of(vararg values: ByteArray): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putBytes(values[pos], 0, values[pos].size)
                }
                override fun size(pos: Int) = values[pos].size
                override fun toString() = values.joinToString()
            }
        }

        public fun of(vararg values: ReadBuffer): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putBytes(values[pos].duplicate())
                }
                override fun size(pos: Int) = values[pos].available
                override fun toString() = values.joinToString()
            }
        }

        public fun of(vararg values: Value): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    val value = values[pos]
                    value.writeInto(dst)
                }
                override fun size(pos: Int) = values[pos].size
                override fun toString() = values.joinToString()
            }
        }

        public fun of(vararg values: Boolean): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putByte((if (values[pos]) 1 else 0).toByte())
                }
                override fun size(pos: Int) = 1
                override fun toString() = values.joinToString()
            }
        }

        public fun of(vararg values: Byte): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putByte(values[pos])
                }
                override fun size(pos: Int) = 1
                override fun toString() = values.joinToString()
            }
        }

        public fun of(vararg values: Short): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putShort(values[pos])
                }
                override fun size(pos: Int) = 2
                override fun toString() = values.joinToString()
            }
        }

        public fun of(vararg values: Int): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putInt(values[pos])
                }
                override fun size(pos: Int) = 4
                override fun toString() = values.joinToString()
            }
        }

        public fun of(vararg values: Long): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putLong(values[pos])
                }
                override fun size(pos: Int) = 8
                override fun toString() = values.joinToString()
            }
        }

        public fun of(vararg values: UUID): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putUUID(values[pos])
                }
                override fun size(pos: Int) = 16
                override fun toString() = values.joinToString()
            }
        }

        public fun ofAscii(vararg values: Char): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putByte(values[pos].toByte())
                }
                override fun size(pos: Int) = 1
                override fun toString() = values.joinToString()
            }
        }


        public fun ofAscii(vararg values: CharSequence): Value {
            return object : Value.ZeroSpacedValues(values.size) {
                override fun write(dst: Writeable, pos: Int) {
                    dst.putAscii(values[pos])
                }
                override fun size(pos: Int) = values[pos].length
                override fun toString() = values.joinToString()
            }
        }

        private fun Any.toValue(): Value = when (this) {
            is Value ->  this
            is ByteArray -> of(this)
            is ReadBuffer -> of(this)
            is Allocation -> of(this)
            is Boolean ->  of((if (this) 1 else 0).toByte())
            is Byte ->  of(this)
            is Char ->  ofAscii(this)
            is Short ->  of(this)
            is Int ->  of(this)
            is Long ->  of(this)
            is UUID -> of(this)
            is String ->  ofAscii(this)
            else -> throw IllegalArgumentException("invalid value: $this")
        }

        public fun ofAll(vararg values: Any): Value {
            if (values.isEmpty())
                return emptyValue

            if (values.size == 1 && values[0] is Value)
                return values[0] as Value

            val sized = Array(values.size) {
                values[it].toValue()
            }

            return of(*sized)
        }

        @Suppress("UNCHECKED_CAST")
        public fun ofAny(value: Any): Value = when (value) {
            is Collection<*> ->
                if (value.size == 1) value.requireNoNulls().first().toValue()
                else ofAll(*value.toTypedArray().requireNoNulls())
            is Array<*> ->
                if (value.size == 1) (value as Array<Any?>).requireNoNulls().first().toValue()
                else ofAll(*(value as Array<Any?>).requireNoNulls())
            else -> value.toValue()
        }
    }

}
