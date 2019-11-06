package org.kodein.db.model

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.Serializer
import org.kodein.memory.io.KBuffer
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.Writeable
import org.kodein.memory.io.arrayCopy
import kotlin.reflect.KClass

data class IntPrimitive(override val id: Value, val value: Int) : Metadata {
    object S : Serializer<IntPrimitive> {
        override fun serialize(model: IntPrimitive, output: Writeable, vararg options: Options.Write) = output.putInt(model.value)
        override fun deserialize(type: KClass<out IntPrimitive>, transientId: ReadBuffer, input: ReadBuffer, vararg options: Options.Read) = IntPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readInt())
    }
}

data class LongPrimitive(override val id: Value, val value: Long) : Metadata {
    object S : Serializer<LongPrimitive> {
        override fun serialize(model: LongPrimitive, output: Writeable, vararg options: Options.Write) = output.putLong(model.value)
        override fun deserialize(type: KClass<out LongPrimitive>, transientId: ReadBuffer, input: ReadBuffer, vararg options: Options.Read) = LongPrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readLong())
    }
}

data class DoublePrimitive(override val id: Value, val value: Double) : Metadata {
    object S : Serializer<DoublePrimitive> {
        override fun serialize(model: DoublePrimitive, output: Writeable, vararg options: Options.Write) = output.putDouble(model.value)
        override fun deserialize(type: KClass<out DoublePrimitive>, transientId: ReadBuffer, input: ReadBuffer, vararg options: Options.Read) = DoublePrimitive(Value.of(KBuffer.arrayCopy(transientId)), input.readDouble())
    }
}

@Suppress("FunctionName")
fun Primitive(id: Value, value: Int) = IntPrimitive(id, value)
@Suppress("FunctionName")
fun Primitive(id: Value, value: Long) = LongPrimitive(id, value)
@Suppress("FunctionName")
fun Primitive(id: Value, value: Double) = DoublePrimitive(id, value)
