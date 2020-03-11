package org.kodein.db.impl.model

import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.Value
import org.kodein.db.data.DataRead
import org.kodein.db.impl.data.getDocumentKeyID
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.ModelRead
import org.kodein.memory.use
import kotlin.reflect.KClass

internal interface ModelReadModule : ModelKeyMakerModule, ModelRead {

    override val data: DataRead

    override fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Read): Sized<M>? {
        return data.get(key.bytes, *options)?.use { mdb.deserialize(type, getDocumentKeyID(key.bytes), it, options) }
    }

    override fun findAll(vararg options: Options.Read): ModelCursor<*> =
            ModelCursorImpl(data.findAll(*options), mdb, Any::class)

    override fun <M : Any> findAllByType(type: KClass<M>, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findAllByType(mdb.getTypeId(mdb.typeTable.getTypeName(type)), *options), mdb, type)

    override fun <M : Any> findById(type: KClass<M>, id: Any, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findById(mdb.getTypeId(mdb.typeTable.getTypeName(type)), Value.ofAny(id), isOpen, *options), mdb, type)

    override fun <M : Any> findAllByIndex(type: KClass<M>, index: String, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findAllByIndex(mdb.getTypeId(mdb.typeTable.getTypeName(type)), index, *options), mdb, type)

    override fun <M : Any> findByIndex(type: KClass<M>, index: String, value: Any, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findByIndex(mdb.getTypeId(mdb.typeTable.getTypeName(type)), index, Value.ofAny(value), isOpen, *options), mdb, type)

    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): Set<String> =
            data.getIndexesOf(key.bytes, *options)
}
