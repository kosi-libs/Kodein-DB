package org.kodein.db.model.orm

import org.kodein.db.Options
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.Writeable
import kotlin.reflect.KClass

public interface Serializer<M : Any> {
    public fun serialize(model: M, output: Writeable, vararg options: Options.Write)
    public fun deserialize(type: KClass<out M>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): M
}

public interface DefaultSerializer : Serializer<Any>, Options.Open