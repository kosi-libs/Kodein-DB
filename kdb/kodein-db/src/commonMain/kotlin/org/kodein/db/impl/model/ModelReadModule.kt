package org.kodein.db.impl.model

import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.Value
import org.kodein.db.ascii.readAscii
import org.kodein.db.data.DataRead
import org.kodein.db.impl.data.getObjectKeyID
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.ModelRead
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.use
import kotlin.reflect.KClass

internal interface ModelReadModule : ModelKeyMakerModule, ModelRead {

    override val data: DataRead

    companion object {
        internal fun <M : Any> getFrom(buffer: ReadBuffer, transientId: ReadBuffer, type: KClass<out M>, mdb: ModelDBImpl, options: Array<out Options.Read>): Sized<M> {
            val typeLength = buffer.readShort()
            val typeName = buffer.readAscii(typeLength.toInt())

            val realType = mdb.typeTable.getTypeClass(typeName) ?: run {
                check(type != Any::class) { "Type $typeName is not declared in type table." }
                val expectedTypeName = mdb.typeTable.getTypeName(type)
                check(typeName == expectedTypeName) { "Type $typeName is not declared in type table and do not match expected type $expectedTypeName." }
                type
            }

            @Suppress("UNCHECKED_CAST")
            realType as KClass<M>

            val r = buffer.remaining

            @Suppress("UNCHECKED_CAST")
            val model = mdb.deserialize(realType, transientId, buffer, *options) as M

            return Sized(model, r - buffer.remaining)
        }

    }

    override fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Read): Sized<M>? {
        return data.get(key.bytes, *options)?.use { getFrom(it, getObjectKeyID(key.bytes), type, mdb, options) }
    }

    override fun findAll(vararg options: Options.Read): ModelCursor<*> =
            ModelCursorImpl(data.findAll(*options), mdb, Any::class)

    override fun <M : Any> findAllByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findAllByType(mdb.typeTable.getTypeName(type), *options), mdb, type)

    override fun <M : Any> findById(type: KClass<M>, id: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findById(mdb.typeTable.getTypeName(type), id, isOpen, *options), mdb, type)

    override fun <M : Any> findAllByIndex(type: KClass<M>, index: String, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findAllByIndex(mdb.typeTable.getTypeName(type), index, *options), mdb, type)

    override fun <M : Any> findByIndex(type: KClass<M>, index: String, value: Value, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findByIndex(mdb.typeTable.getTypeName(type), index, value, isOpen, *options), mdb, type)

    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): List<String> =
            data.getIndexesOf(key.bytes, *options)
}
