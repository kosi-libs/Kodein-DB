package org.kodein.db.orm.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer
import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.model.orm.DefaultSerializer
import org.kodein.memory.io.*
import org.kodein.memory.use
import org.objenesis.strategy.StdInstantiatorStrategy
import kotlin.reflect.KClass


@Suppress("unused", "MemberVisibilityCanBePrivate")
public class KryoSerializer @JvmOverloads public constructor(public val kryo: Kryo = createKryo()) : DefaultSerializer {

    override fun serialize(model: Any, output: Writeable, vararg options: Options.Write) {
        Output(output.asOuputStream()).use {
            kryo.writeObject(it, model)
        }
    }

    override fun deserialize(type: KClass<out Any>, transientId: ReadMemory, input: ReadBuffer, vararg options: Options.Read): Any {
        Input(input.asInputStream()).use {
            return kryo.readObject(it, type.java)
        }
    }

    public companion object {
        public fun createKryo(
                typeTable: TypeTable? = null,
                allowStructureUpdate: Boolean = true,
                allowDeserializationWithoutConstructor: Boolean = true
        ): Kryo = Kryo().apply {
            val registered = typeTable?.getRegisteredClasses()
            if (registered != null && registered.isNotEmpty()) {
                registered.forEach { register(it.java) }
            } else {
                isRegistrationRequired = false
            }

            if (allowStructureUpdate) {
                setDefaultSerializer(CompatibleFieldSerializer::class.java)
            }

            if (allowDeserializationWithoutConstructor) {
                instantiatorStrategy = Kryo.DefaultInstantiatorStrategy(StdInstantiatorStrategy())
            }
        }
    }
}
