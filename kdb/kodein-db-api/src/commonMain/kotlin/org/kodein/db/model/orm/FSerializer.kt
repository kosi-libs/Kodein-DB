package org.kodein.db.model.orm

import org.kodein.db.Options
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.Writeable
import kotlin.reflect.KClass


public class FSerializer<M : Any>(private val serialize: Writeable.(M) -> Unit, private val deserialize: ReadBuffer.(KClass<out M>) -> M) : Serializer<M> {

    override fun serialize(model: M, output: Writeable, vararg options: Options.Write): Unit = output.serialize(model)

    override fun deserialize(type: KClass<out M>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): M = input.deserialize(type)
}

