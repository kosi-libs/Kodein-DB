package org.kodein.db.impl.model

import org.kodein.db.Body
import org.kodein.db.Options
import org.kodein.db.ascii.putAscii
import org.kodein.db.data.DataWrite
import org.kodein.db.model.Key
import org.kodein.db.model.Metadata
import org.kodein.db.model.ModelWrite
import org.kodein.db.react.DBListener
import org.kodein.memory.cache.Sized
import org.kodein.memory.io.Allocation
import org.kodein.memory.util.forEachResilient

internal interface BaseModelWrite : BaseModelBase, ModelWrite {

    override val data: DataWrite

    fun didAction(action: DBListener.() -> Unit)

    private inline fun <M: Any, T> put(model: M, options: Array<out Options.Write>, block: (Metadata, Body) -> T): T {
        val metadata = mdb.getMetadata(model, options)
        val typeName = mdb.typeTable.getTypeName(model::class)
        mdb.listeners.toList().forEach { it.willPut(model, typeName, metadata) }
        val body = Body {
            it.putShort(typeName.length.toShort())
            it.putAscii(typeName)
            mdb.serializer.serialize(model, it, *options)
        }
        val ret = block(metadata, body)
        didAction { didPut(model, typeName, metadata) }
        return ret
    }

    override fun put(model: Any, vararg options: Options.Write): Int {
        return put(model, options) { metadata, body ->
            data.put(mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class), metadata.primaryKey, body, metadata.indexes, *options)
        }
    }

    override fun <M : Any> putAndGetHeapKey(model: M, vararg options: Options.Write): Sized<Key<M>> {
        return put(model, options) { metadata, body ->
            val (key, size) = data.putAndGetHeapKey(mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class), metadata.primaryKey, body, metadata.indexes, *options)
            Sized(Key.Heap(model::class, key) as Key<M>, size)
        }
    }

    override fun <M : Any> putAndGetNativeKey(model: M, vararg options: Options.Write): Sized<Key.Native<M>> {
        var alloc: Allocation? = null
        try {
            return put(model, options) { metadata, body ->
                val (key, size) = data.putAndGetNativeKey(mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class), metadata.primaryKey, body, metadata.indexes, *options)
                alloc = key
                Sized(Key.Native(model::class, key), size)
            }
        } catch (t: Throwable) {
            alloc?.close()
            throw t
        }
    }

    private object UNFETCHED

    override fun delete(key: Key<*>, vararg options: Options.Write) {
        val typeName = mdb.typeTable.getTypeName(key.type)
        var model: Any? = UNFETCHED
        val get: () -> Any? = {
            if (model == UNFETCHED)
                model = mdb[key]?.value
            model
        }
        mdb.listeners.toList().forEach { it.willDelete(key, typeName, get)}
        data.delete(key.bytes, *options)
        didAction { didDelete(key, typeName) }
    }
}
