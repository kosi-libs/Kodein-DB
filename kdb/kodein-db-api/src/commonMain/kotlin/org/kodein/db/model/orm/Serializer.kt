package org.kodein.db.model.orm

import org.kodein.db.Options
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.Writeable
import kotlin.reflect.KClass

interface Serializer<M : Any> {
    fun serialize(model: M, output: Writeable, vararg options: Options.Write)
    fun deserialize(type: KClass<out M>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): M
}

interface DefaultSerializer : Serializer<Any>, Options.Open