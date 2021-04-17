package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.Serializer
import org.kodein.memory.io.*
import org.kodein.memory.text.Charset
import org.kodein.memory.text.writeString
import org.kodein.memory.text.readString
import kotlin.reflect.KClass

public data class IntPrimitive(override val id: Any, val value: Int) : Metadata {
    public object S : Serializer<IntPrimitive> {
        override fun serialize(model: IntPrimitive, output: Writeable, vararg options: Options.Write) { output.writeInt(model.value) }
        override fun deserialize(type: KClass<out IntPrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Read): IntPrimitive = IntPrimitive(Value.of(Memory.arrayCopy(transientId)), input.readInt())
    }
}

public data class LongPrimitive(override val id: Any, val value: Long) : Metadata {
    public object S : Serializer<LongPrimitive> {
        override fun serialize(model: LongPrimitive, output: Writeable, vararg options: Options.Write) { output.writeLong(model.value) }
        override fun deserialize(type: KClass<out LongPrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Read): LongPrimitive = LongPrimitive(Value.of(Memory.arrayCopy(transientId)), input.readLong())
    }
}

public data class DoublePrimitive(override val id: Any, val value: Double) : Metadata {
    public object S : Serializer<DoublePrimitive> {
        override fun serialize(model: DoublePrimitive, output: Writeable, vararg options: Options.Write) { output.writeDouble(model.value) }
        override fun deserialize(type: KClass<out DoublePrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Read): DoublePrimitive = DoublePrimitive(Value.of(Memory.arrayCopy(transientId)), input.readDouble())
    }
}

public data class StringPrimitive(override val id: Any, val value: String) : Metadata {
    public object S : Serializer<StringPrimitive> {
        override fun serialize(model: StringPrimitive, output: Writeable, vararg options: Options.Write) { output.writeString(model.value, charset = Charset.UTF8) }
        override fun deserialize(type: KClass<out StringPrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Read): StringPrimitive = StringPrimitive(Value.of(Memory.arrayCopy(transientId)), input.readString(charset = Charset.UTF8))
    }
}

public data class BytesPrimitive(override val id: Any, val value: ByteArray) : Metadata {
    public object S : Serializer<BytesPrimitive> {
        override fun serialize(model: BytesPrimitive, output: Writeable, vararg options: Options.Write) { output.writeBytes(model.value) }
        override fun deserialize(type: KClass<out BytesPrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Read): BytesPrimitive = BytesPrimitive(Value.of(Memory.arrayCopy(transientId)), input.readBytes())
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
