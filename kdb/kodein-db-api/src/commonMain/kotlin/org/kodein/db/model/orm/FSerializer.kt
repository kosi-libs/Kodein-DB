package org.kodein.db.model.orm

import org.kodein.db.Options
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.Writeable
import kotlin.reflect.KClass


class FSerializer<M : Any>(private val serialize: Writeable.(M) -> Unit, private val deserialize: ReadBuffer.(KClass<out M>) -> M) : Serializer<M> {

    override fun serialize(model: M, output: Writeable, vararg options: Options.Write) = output.serialize(model)

    @Suppress("UNCHECKED_CAST")
    override fun <R : M> deserialize(type: KClass<R>, input: ReadBuffer, vararg options: Options.Read): R = input.deserialize(type) as R
}

