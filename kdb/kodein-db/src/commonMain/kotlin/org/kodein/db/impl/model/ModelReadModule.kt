package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.ascii.readAscii
import org.kodein.db.data.DataRead
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.ModelRead
import org.kodein.db.model.orm.Serializer
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.use
import kotlin.reflect.KClass

internal interface ModelReadModule : ModelKeyMakerModule, ModelRead {

    override val data: DataRead

    companion object {
        internal fun <M : Any> getFrom(buffer: ReadBuffer, type: KClass<out M>, typeTable: TypeTable, serializer: Serializer<Any>, options: Array<out Options.Read>): Sized<M> {
            val typeLength = buffer.readShort()
            val typeName = buffer.readAscii(typeLength.toInt())

            val realType = typeTable.getTypeClass(typeName) ?: run {
                if (type == Any::class)
                    throw IllegalStateException("Type $typeName is not declared in type table.")
                val expectedTypeName = typeTable.getTypeName(type)
                if (typeName != expectedTypeName)
                    throw IllegalStateException("Type $typeName is not declared in type table and do not match expected type $expectedTypeName.")
                type
            }

            @Suppress("UNCHECKED_CAST")
            realType as KClass<M>

            val r = buffer.remaining

            val model = serializer.deserialize(realType, buffer, *options)

            return Sized(model, r - buffer.remaining)
        }
    }

    override fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Read): Sized<M>? {
        return data.get(key.bytes, *options)?.use { getFrom(it, type, mdb.typeTable, mdb.serializer, options) }
    }

    override fun findAll(vararg options: Options.Read): ModelCursor<*> =
            ModelCursorImpl(data.findAll(*options), mdb.typeTable, mdb.serializer, Any::class)

    override fun <M : Any> findAllByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findAllByType(mdb.typeTable.getTypeName(type), *options), mdb.typeTable, mdb.serializer, type)

    override fun <M : Any> findByPrimaryKey(type: KClass<M>, primaryKey: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findByPrimaryKey(mdb.typeTable.getTypeName(type), primaryKey, isOpen, *options), mdb.typeTable, mdb.serializer, type)

    override fun <M : Any> findAllByIndex(type: KClass<M>, name: String, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findAllByIndex(mdb.typeTable.getTypeName(type), name, *options), mdb.typeTable, mdb.serializer, type)

    override fun <M : Any> findByIndex(type: KClass<M>, name: String, value: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findByIndex(mdb.typeTable.getTypeName(type), name, value, isOpen, *options), mdb.typeTable, mdb.serializer, type)

    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String> =
            data.getIndexesOf(key.bytes, *options)
}
