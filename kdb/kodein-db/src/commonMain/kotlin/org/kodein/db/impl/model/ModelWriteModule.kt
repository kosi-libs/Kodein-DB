package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataIndexMap
import org.kodein.db.data.DataWrite
import org.kodein.db.impl.data.writeDocumentKey
import org.kodein.db.model.ModelIndexData
import org.kodein.db.model.ModelWrite
import org.kodein.db.model.orm.Metadata
import org.kodein.memory.io.ReadMemory
import org.kodein.memory.io.asReadable
import org.kodein.memory.io.verify
import kotlin.reflect.KClass


internal typealias DataPut<D, O> = D.(ReadMemory, Body, DataIndexMap, Array<out O>) -> Int
internal typealias DataDelete<D, O> = D.(ReadMemory, Array<out O>) -> Unit

internal interface ModelWriteModule<D : DataWrite> : ModelKeyMakerModule, ModelWrite {

    override val data: D

    fun willAction(action: DBListener<Any>.() -> Unit)

    fun didAction(action: DBListener<Any>.() -> Unit)

    private fun <M: Any, O: Options> put(model: M, options: Array<out O>, dataPut: DataPut<D, O>, getKey: (ReadMemory, Metadata) -> Key<M>): KeyAndSize<M> {
        val putsOptions = options.filterIsInstance<Options.Puts>().toTypedArray()
        val metadata = mdb.getMetadata(model, putsOptions)
        val typeName = mdb.typeTable.getTypeName(model::class)
        val rootTypeName = mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class)
        willAction { willPut(model, rootTypeName, metadata, putsOptions) }
        val body = Body { body ->
            val typeId = mdb.getTypeId(typeName)
            body.writeInt(typeId)
            mdb.serialize(model, body, *putsOptions)
        }
        val key = getKey(rootTypeName, metadata)
        val indexMap = metadata.indexes().mapValues { (_, data) ->
            when (data) {
                is ModelIndexData -> {
                    data.values.map { (value, associatedData) ->
                        valueOf(value) to associatedData
                    }
                }
                else -> listOf<Pair<Value, Body?>>(valueOf(data) to null)
            }
        }
        val size = data.dataPut(key.bytes, body, indexMap, options)
        didAction { didPut(model, key, rootTypeName, metadata, size, putsOptions) }
        return KeyAndSize(key, size)
    }

    fun <M : Any, O: Options> put(model: M, options: Array<out O>, dataPut: DataPut<D, O>): KeyAndSize<M> =
        put(model, options, dataPut) { rootTypeName, metadata ->
            val key = Key<M>(data.newKey(mdb.getTypeId(rootTypeName), valueOf(metadata.id)))
            key
        }

    fun <M : Any, O: Options> put(key: Key<M>, model: M, options: Array<out O>, dataPut: DataPut<D, O>): Int =
        put(model, options, dataPut) { rootTypeName, metadata ->
            verify(key.bytes.asReadable()) { writeDocumentKey(mdb.getTypeId(rootTypeName), valueOf(metadata.id)) }
            key
        }.size

    fun <M: Any, O: Options> delete(type: KClass<M>, key: Key<M>, options: Array<out O>, dataDelete: DataDelete<D, O>) {
        var fetched = false
        var model: Any? = null
        val getModel: () -> Any? = {
            if (!fetched) {
                model = mdb.get(type, key)?.model
                fetched = true
            }
            model
        }
        val rootTypeName = mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(type) ?: type)
        val deletesOptions = options.filterIsInstance<Options.Deletes>().toTypedArray()
        willAction { willDelete(key, getModel, rootTypeName, deletesOptions) }
        data.dataDelete(key.bytes, options)
        didAction { didDelete(key, model, rootTypeName, deletesOptions) }
    }
}
