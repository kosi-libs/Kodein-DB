package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.Serializer
import org.kodein.memory.io.*
import org.kodein.memory.text.Charset
import org.kodein.memory.text.putString
import org.kodein.memory.text.readString
import kotlin.reflect.KClass

data class IntPrimitive(override val id: Value, val value: Int) : Metadata {
    constructor(id: Any, value: Int) : this(Value.ofAny(id), value)
    object S : Serializer<IntPrimitive> {
        override fun serialize(model: IntPrimitive, output: Writeable, vararg options: Options.Write) { output.putInt(model.value) }
        override fun deserialize(type: KClass<out IntPrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read) = IntPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readInt())
    }
}

data class LongPrimitive(override val id: Value, val value: Long) : Metadata {
    constructor(id: Any, value: Long) : this(Value.ofAny(id), value)
    object S : Serializer<LongPrimitive> {
        override fun serialize(model: LongPrimitive, output: Writeable, vararg options: Options.Write) { output.putLong(model.value) }
        override fun deserialize(type: KClass<out LongPrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read) = LongPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readLong())
    }
}

data class DoublePrimitive(override val id: Value, val value: Double) : Metadata {
    constructor(id: Any, value: Double) : this(Value.ofAny(id), value)
    object S : Serializer<DoublePrimitive> {
        override fun serialize(model: DoublePrimitive, output: Writeable, vararg options: Options.Write) { output.putDouble(model.value) }
        override fun deserialize(type: KClass<out DoublePrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read) = DoublePrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readDouble())
    }
}

data class StringPrimitive(override val id: Value, val value: String) : Metadata {
    constructor(id: Any, value: String) : this(Value.ofAny(id), value)
    object S : Serializer<StringPrimitive> {
        override fun serialize(model: StringPrimitive, output: Writeable, vararg options: Options.Write) { output.putString(model.value, Charset.UTF8) }
        override fun deserialize(type: KClass<out StringPrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read) = StringPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readString(Charset.UTF8))
    }
}

data class BytesPrimitive(override val id: Value, val value: ByteArray) : Metadata {
    constructor(id: Any, value: ByteArray) : this(Value.ofAny(id), value)
    object S : Serializer<BytesPrimitive> {
        override fun serialize(model: BytesPrimitive, output: Writeable, vararg options: Options.Write) { output.putBytes(model.value) }
        override fun deserialize(type: KClass<out BytesPrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read) = BytesPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readBytes())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is BytesPrimitive) return false
        return id == other.id && value.contentEquals(other.value)
    }

    override fun hashCode(): Int = 31 * id.hashCode() + value.contentHashCode()
}

@Suppress("FunctionName")
fun Primitive(id: Any, value: Int) = IntPrimitive(id, value)
@Suppress("FunctionName")
fun Primitive(id: Any, value: Long) = LongPrimitive(id, value)
@Suppress("FunctionName")
fun Primitive(id: Any, value: Double) = DoublePrimitive(id, value)
@Suppress("FunctionName")
fun Primitive(id: Any, value: String) = StringPrimitive(id, value)
@Suppress("FunctionName")
fun Primitive(id: Any, value: ByteArray) = BytesPrimitive(id, value)
