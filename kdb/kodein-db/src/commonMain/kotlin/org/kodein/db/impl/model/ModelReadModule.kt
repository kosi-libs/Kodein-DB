package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataRead
import org.kodein.db.impl.data.getDocumentKeyID
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.ModelRead
import org.kodein.memory.use
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

internal interface ModelReadModule : ModelKeyMakerModule, ModelRead {

    override val data: DataRead

    override fun <M : Any> get(type: TKType<M>, key: Key<M>, vararg options: Options.Read): Sized<M>? {
        return data.get(key.bytes, *options)?.use { mdb.deserialize(type.ktype, getDocumentKeyID(key.bytes), it, options) }
    }

    override fun findAll(vararg options: Options.Read): ModelCursor<*> =
            ModelCursorImpl(data.findAll(*options), mdb, tTypeOf())

    override fun <M : Any> findAllByType(type: TKType<M>, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findAllByType(mdb.getTypeId(mdb.typeTable.getTypeName(type.ktype)), *options), mdb, type)

    override fun <M : Any> findById(type: TKType<M>, id: Any, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findById(mdb.getTypeId(mdb.typeTable.getTypeName(type.ktype)), Value.ofAny(id), isOpen, *options), mdb, type)

    override fun <M : Any> findAllByIndex(type: TKType<M>, index: String, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findAllByIndex(mdb.getTypeId(mdb.typeTable.getTypeName(type.ktype)), index, *options), mdb, type)

    override fun <M : Any> findByIndex(type: TKType<M>, index: String, value: Any, isOpen: Boolean, vararg options: Options.Read): ModelCursor<M> =
            ModelCursorImpl(data.findByIndex(mdb.getTypeId(mdb.typeTable.getTypeName(type.ktype)), index, Value.ofAny(value), isOpen, *options), mdb, type)

    override fun getIndexesOf(key: Key<*>, vararg options: Options.Read): Set<String> =
            data.getIndexesOf(key.bytes, *options)
}
