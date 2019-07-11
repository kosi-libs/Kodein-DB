package org.kodein.db.orm.kryo

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy
import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.model.Serializer
import org.kodein.memory.io.Readable
import org.kodein.memory.io.Writeable
import org.kodein.memory.io.asInputStream
import org.kodein.memory.io.asOuputStream
import org.kodein.memory.use
import org.objenesis.strategy.StdInstantiatorStrategy
import kotlin.reflect.KClass


class KryoSerializer @JvmOverloads constructor(val kryo: Kryo = createKryo()) : Serializer {

    override fun <M : Any> serialize(model: M, output: Writeable, vararg options: Options.Write) {
        Output(output.asOuputStream()).use {
            kryo.writeObject(it, model)
        }
    }

    override fun <M : Any> deserialize(type: KClass<M>, input: Readable, vararg options: Options.Read): M {
        Input(input.asInputStream()).use {
            return kryo.readObject<M>(it, type.java)
        }
    }


    companion object {
        fun createKryo(
                typeTable: TypeTable? = null,
                allowStructureUpdate: Boolean = true,
                allowDeserializationWithoutConstructor: Boolean = true
        ) = Kryo().apply {
            if (typeTable != null) {
                typeTable.getRegisteredClasses().forEach { register(it.java) }
            } else {
                isRegistrationRequired = false
            }

            if (allowStructureUpdate) {
                setDefaultSerializer(CompatibleFieldSerializer::class.java)
            }

            if (allowDeserializationWithoutConstructor) {
                instantiatorStrategy = DefaultInstantiatorStrategy(StdInstantiatorStrategy())
            }
        }
    }
}
