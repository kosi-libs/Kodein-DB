@file:Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION", "DEPRECATION_ERROR", "OverridingDeprecatedMember")

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


// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public interface Primitive : Metadata

// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public data class IntPrimitive(override val id: Any, val value: Int) : Primitive {
    public object S : Serializer<IntPrimitive> {
        override fun serialize(model: IntPrimitive, output: Writeable, vararg options: Options.Puts) { output.writeInt(model.value) }
        override fun deserialize(type: KClass<out IntPrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Get): IntPrimitive = IntPrimitive(Value.of(Memory.arrayCopy(transientId)), input.readInt())
        override fun deserialize(type: KClass<out IntPrimitive>, input: CursorReadable, vararg options: Options.Get): IntPrimitive = error("Primitives can only be serialized with transientId")
    }
}

// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public data class LongPrimitive(override val id: Any, val value: Long) : Primitive {
    public object S : Serializer<LongPrimitive> {
        override fun serialize(model: LongPrimitive, output: Writeable, vararg options: Options.Puts) { output.writeLong(model.value) }
        override fun deserialize(type: KClass<out LongPrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Get): LongPrimitive = LongPrimitive(Value.of(Memory.arrayCopy(transientId)), input.readLong())
        override fun deserialize(type: KClass<out LongPrimitive>, input: CursorReadable, vararg options: Options.Get): LongPrimitive = error("Primitives can only be serialized with transientId")
    }
}

// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public data class DoublePrimitive(override val id: Any, val value: Double) : Primitive {
    public object S : Serializer<DoublePrimitive> {
        override fun serialize(model: DoublePrimitive, output: Writeable, vararg options: Options.Puts) { output.writeDouble(model.value) }
        override fun deserialize(type: KClass<out DoublePrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Get): DoublePrimitive = DoublePrimitive(Value.of(Memory.arrayCopy(transientId)), input.readDouble())
        override fun deserialize(type: KClass<out DoublePrimitive>, input: CursorReadable, vararg options: Options.Get): DoublePrimitive = error("Primitives can only be serialized with transientId")
    }
}

// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public data class StringPrimitive(override val id: Any, val value: String) : Primitive {
    public object S : Serializer<StringPrimitive> {
        override fun serialize(model: StringPrimitive, output: Writeable, vararg options: Options.Puts) { output.writeString(model.value, charset = Charset.UTF8) }
        override fun deserialize(type: KClass<out StringPrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Get): StringPrimitive = StringPrimitive(Value.of(Memory.arrayCopy(transientId)), input.readString(charset = Charset.UTF8))
        override fun deserialize(type: KClass<out StringPrimitive>, input: CursorReadable, vararg options: Options.Get): StringPrimitive = error("Primitives can only be serialized with transientId")
    }
}

// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public data class BytesPrimitive(override val id: Any, val value: ByteArray) : Primitive {
    public object S : Serializer<BytesPrimitive> {
        override fun serialize(model: BytesPrimitive, output: Writeable, vararg options: Options.Puts) { output.writeBytes(model.value) }
        override fun deserialize(type: KClass<out BytesPrimitive>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Get): BytesPrimitive = BytesPrimitive(Value.of(Memory.arrayCopy(transientId)), input.readBytes())
        override fun deserialize(type: KClass<out BytesPrimitive>, input: CursorReadable, vararg options: Options.Get): BytesPrimitive = error("Primitives can only be serialized with transientId")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is BytesPrimitive) return false
        return id == other.id && value.contentEquals(other.value)
    }

    override fun hashCode(): Int = 31 * id.hashCode() + value.contentHashCode()
}

@Suppress("FunctionName")
// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public fun Primitive(id: Any, value: Int): IntPrimitive = IntPrimitive(id, value)

@Suppress("FunctionName")
// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public fun Primitive(id: Any, value: Long): LongPrimitive = LongPrimitive(id, value)

@Suppress("FunctionName")
// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public fun Primitive(id: Any, value: Double): DoublePrimitive = DoublePrimitive(id, value)

@Suppress("FunctionName")
// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public fun Primitive(id: Any, value: String): StringPrimitive = StringPrimitive(id, value)

@Suppress("FunctionName")
// Deprecated since version 0.8.0
@Deprecated(message = "Primitives are disabled. You should create your own value classes.", level = DeprecationLevel.ERROR)
public fun Primitive(id: Any, value: ByteArray): BytesPrimitive = BytesPrimitive(id, value)
