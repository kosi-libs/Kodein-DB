package org.kodein.db.impl.model

import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.Value
import org.kodein.db.data.DataRead
import org.kodein.db.impl.data.getDocumentKeyID
import org.kodein.db.model.ModelCursor
import org.kodein.db.model.ModelIndexCursor
import org.kodein.db.model.ModelRead
import org.kodein.memory.use
import kotlin.reflect.KClass

internal interface ModelReadModule : ModelKeyMakerModule, ModelRead {

    override val data: DataRead

    override fun <M : Any> get(type: KClass<M>, key: Key<M>, vararg options: Options.Get): Sized<M>? {
        return data.get(key.bytes, *options)?.use { mdb.deserialize(type, getDocumentKeyID(key.bytes), it, options) }
    }

    override fun findAll(vararg options: Options.Find): ModelCursor<*> =
            ModelCursorImpl(data.findAll(*options), mdb, Any::class)

    private fun checkIsRoot(type: KClass<*>) {
        val root = mdb.typeTable.getRootOf(type)
        if (root != null && root != type) error("${type.simpleName} is a sub type of ${root.simpleName}. You must find by ${root.simpleName}.")
    }

    override fun <M : Any> findAllByType(type: KClass<M>, vararg options: Options.Find): ModelCursor<M> {
        checkIsRoot(type)
        return ModelCursorImpl(data.findAllByType(mdb.getTypeId(mdb.typeTable.getTypeName(type)), *options), mdb, type)
    }

    override fun <M : Any> findById(type: KClass<M>, id: Any, isOpen: Boolean, vararg options: Options.Find): ModelCursor<M> {
        checkIsRoot(type)
        return ModelCursorImpl(data.findById(mdb.getTypeId(mdb.typeTable.getTypeName(type)), valueOf(id), isOpen, *options), mdb, type)
    }

    override fun <M : Any> findAllByIndex(type: KClass<M>, index: String, vararg options: Options.Find): ModelIndexCursor<M> {
        checkIsRoot(type)
        return ModelIndexCursorImpl(data.findAllByIndex(mdb.getTypeId(mdb.typeTable.getTypeName(type)), index, *options), mdb, type)
    }

    override fun <M : Any> findByIndex(type: KClass<M>, index: String, value: Any, isOpen: Boolean, vararg options: Options.Find): ModelIndexCursor<M> {
        checkIsRoot(type)
        return ModelIndexCursorImpl(data.findByIndex(mdb.getTypeId(mdb.typeTable.getTypeName(type)), index, valueOf(value), isOpen, *options), mdb, type)
    }

    override fun getIndexesOf(key: Key<*>): Set<String> =
            data.getIndexesOf(key.bytes)
}
