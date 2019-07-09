package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.Value
import org.kodein.db.ascii.readAscii
import org.kodein.db.data.DataRead
import org.kodein.db.model.*
import org.kodein.memory.Readable
import org.kodein.memory.model.Sized
import org.kodein.memory.use
import kotlin.reflect.KClass

internal interface BaseModelRead : BaseModelBase, ModelRead {

    override val data: DataRead

    companion object {
        internal fun <M : Any> getFrom(readable: Readable, type: KClass<out M>, typeTable: TypeTable, serializer: Serializer, options: Array<out Options.Read>): Sized<M> {
            val typeLength = readable.readShort()
            val typeName = readable.readAscii(typeLength.toInt())

            val realType = typeTable.getTypeClass(typeName) ?: run {
                if (typeName != typeTable.getTypeName(type))
                    throw IllegalStateException("Could not find type $typeName (parameter type $type do not match)")
                type
            }

            @Suppress("UNCHECKED_CAST")
            realType as KClass<M>

            val r = readable.remaining

            val model = serializer.deserialize(realType, readable, *options)

            return Sized(model, r - readable.remaining)
        }
    }

    override fun <M : Any> get(key: Key<M>, vararg options: Options.Read): Sized<M>? {
        return data.get(key.bytes, *options)?.use { getFrom(it, key.type, mdb.typeTable, mdb.serializer, options) }
    }

    override fun findAll(vararg options: Options.Read): ModelCursor<*> =
            ModelCursorImpl(data.findAll(*options), mdb.typeTable, mdb.serializer, Any::class)

    override fun <M : Any> findByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M> =
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
