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

public data class IntPrimitive(override val id: Value, val value: Int) : Metadata {
    public constructor(id: Any, value: Int) : this(Value.ofAny(id), value)
    public object S : Serializer<IntPrimitive> {
        override fun serialize(model: IntPrimitive, output: Writeable, vararg options: Options.Write) { output.putInt(model.value) }
        override fun deserialize(type: KClass<out IntPrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): IntPrimitive = IntPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readInt())
    }
}

public data class LongPrimitive(override val id: Value, val value: Long) : Metadata {
    public constructor(id: Any, value: Long) : this(Value.ofAny(id), value)
    public object S : Serializer<LongPrimitive> {
        override fun serialize(model: LongPrimitive, output: Writeable, vararg options: Options.Write) { output.putLong(model.value) }
        override fun deserialize(type: KClass<out LongPrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): LongPrimitive = LongPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readLong())
    }
}

public data class DoublePrimitive(override val id: Value, val value: Double) : Metadata {
    public constructor(id: Any, value: Double) : this(Value.ofAny(id), value)
    public object S : Serializer<DoublePrimitive> {
        override fun serialize(model: DoublePrimitive, output: Writeable, vararg options: Options.Write) { output.putDouble(model.value) }
        override fun deserialize(type: KClass<out DoublePrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): DoublePrimitive = DoublePrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readDouble())
    }
}

public data class StringPrimitive(override val id: Value, val value: String) : Metadata {
    public constructor(id: Any, value: String) : this(Value.ofAny(id), value)
    public object S : Serializer<StringPrimitive> {
        override fun serialize(model: StringPrimitive, output: Writeable, vararg options: Options.Write) { output.putString(model.value, Charset.UTF8) }
        override fun deserialize(type: KClass<out StringPrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): StringPrimitive = StringPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readString(Charset.UTF8))
    }
}

public data class BytesPrimitive(override val id: Value, val value: ByteArray) : Metadata {
    public constructor(id: Any, value: ByteArray) : this(Value.ofAny(id), value)
    public object S : Serializer<BytesPrimitive> {
        override fun serialize(model: BytesPrimitive, output: Writeable, vararg options: Options.Write) { output.putBytes(model.value) }
        override fun deserialize(type: KClass<out BytesPrimitive>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): BytesPrimitive = BytesPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readBytes())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is BytesPrimitive) return false
        return id == other.id && value.contentEquals(other.value)
    }

    override fun hashCode(): Int = 31 * id.hashCode() + value.contentHashCode()
}

@Suppress("FunctionName")
public fun Primitive(id: Any, value: Int): IntPrimitive = IntPrimitive(id, value)
@Suppress("FunctionName")
public fun Primitive(id: Any, value: Long): LongPrimitive = LongPrimitive(id, value)
@Suppress("FunctionName")
public fun Primitive(id: Any, value: Double): DoublePrimitive = DoublePrimitive(id, value)
@Suppress("FunctionName")
public fun Primitive(id: Any, value: String): StringPrimitive = StringPrimitive(id, value)
@Suppress("FunctionName")
public fun Primitive(id: Any, value: ByteArray): BytesPrimitive = BytesPrimitive(id, value)
