package org.kodein.db.model.orm

import org.kodein.db.Options
import org.kodein.memory.io.CursorReadable
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.Writeable
import kotlin.reflect.KClass

public interface Serializer<M : Any> {
    public fun serialize(model: M, output: Writeable, vararg options: Options.Puts)

    // Deprecated since version 0.8.0
    @Deprecated("Accessing IDs in deserialization", replaceWith = ReplaceWith("deserialize(type, input, *options)"), level = DeprecationLevel.ERROR)
    public fun deserialize(type: KClass<out M>, transientId: ReadMemory, input: CursorReadable, vararg options: Options.Get): M = deserialize(type, input, *options)
    public fun deserialize(type: KClass<out M>, input: CursorReadable, vararg options: Options.Get): M
}

public interface DefaultSerializer : Serializer<Any>, Options.Open