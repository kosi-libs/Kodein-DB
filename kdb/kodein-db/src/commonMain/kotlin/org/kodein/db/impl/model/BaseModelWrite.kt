package org.kodein.db.impl.model

import org.kodein.db.Body
import org.kodein.db.Options
import org.kodein.db.ascii.putAscii
import org.kodein.db.data.DataWrite
import org.kodein.db.model.Key
import org.kodein.db.model.Metadata
import org.kodein.db.model.ModelWrite
import org.kodein.db.model.DBListener
import org.kodein.memory.cache.Sized
import org.kodein.memory.io.Allocation

internal interface BaseModelWrite : BaseModelBase, ModelWrite {

    override val data: DataWrite

    fun Key<*>.transform(): Key<*> = this

    fun didAction(action: DBListener.() -> Unit)

    private inline fun <M: Any, T> put(model: M, options: Array<out Options.Write>, block: (Metadata, Body) -> Triple<T, Key<*>?, Int>): T {
        val metadata = mdb.getMetadata(model, options)
        val typeName = mdb.typeTable.getTypeName(model::class)
        mdb.listeners.toList().forEach { it.willPut(model, typeName, metadata, options) }
        val body = Body {
            it.putShort(typeName.length.toShort())
            it.putAscii(typeName)
            mdb.serializer.serialize(model, it, *options)
        }
        val (ret, k, size) = block(metadata, body)
        var key = k
        val getKey: () -> Key<*> = {
            if (key == null)
                key = Key.Heap(model::class, data.getHeapKey(typeName, metadata.primaryKey))
            key!!
        }
        didAction { didPut(model, getKey, typeName, metadata, size, options) }
        return ret
    }

    override fun put(model: Any, vararg options: Options.Write): Int {
        return put(model, options) { metadata, body ->
            val size = data.put(mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class), metadata.primaryKey, body, metadata.indexes, *options)
            Triple(size, null, size)
        }
    }

    override fun <M : Any> putAndGetHeapKey(model: M, vararg options: Options.Write): Sized<Key<M>> {
        return put(model, options) { metadata, body ->
            val (keyBuffer, size) = data.putAndGetHeapKey(mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class), metadata.primaryKey, body, metadata.indexes, *options)
            val key = Key.Heap(model::class, keyBuffer) as Key<M>
            Triple(Sized(key, size), key, size)
        }
    }

    override fun <M : Any> putAndGetNativeKey(model: M, vararg options: Options.Write): Sized<Key.Native<M>> {
        var alloc: Allocation? = null
        try {
            return put(model, options) { metadata, body ->
                val (keyBuffer, size) = data.putAndGetNativeKey(mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class), metadata.primaryKey, body, metadata.indexes, *options)
                alloc = keyBuffer
                val key = Key.Native(model::class, keyBuffer)
                Triple(Sized(key, size), key, size)
            }
        } catch (t: Throwable) {
            alloc?.close()
            throw t
        }
    }

    override fun delete(key: Key<*>, vararg options: Options.Write) {
        val typeName = mdb.typeTable.getTypeName(key.type)
        var fetched = false
        var model: Any? = null
        val getModel: () -> Any? = {
            if (!fetched) {
                model = mdb[key]?.value
                fetched = true
            }
            model
        }
        mdb.listeners.toList().forEach { it.willDelete(key, getModel, typeName, options)}
        data.delete(key.bytes, *options)
        didAction { didDelete(key, model, typeName, options) }
    }
}
