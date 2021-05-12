package org.kodein.db.model.orm

import org.kodein.db.Options
import org.kodein.memory.io.CursorReadable
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.Writeable
import kotlin.reflect.KClass


public class FSerializer<M : Any>(private val serialize: Writeable.(M) -> Unit, private val deserialize: CursorReadable.(KClass<out M>) -> M) : Serializer<M> {

    override fun serialize(model: M, output: Writeable, vararg options: Options.Puts): Unit = output.serialize(model)

    override fun deserialize(type: KClass<out M>, input: CursorReadable, vararg options: Options.Get): M = input.deserialize(type)
}

