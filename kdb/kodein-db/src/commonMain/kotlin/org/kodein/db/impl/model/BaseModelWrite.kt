package org.kodein.db.impl.model

import org.kodein.db.Body
import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.ascii.putAscii
import org.kodein.db.data.DataRead
import org.kodein.db.data.DataWrite
import org.kodein.db.model.*
import org.kodein.memory.model.Sized

internal interface BaseModelWrite : BaseModelBase, ModelWrite {

    override val data: DataWrite

    private inline fun <M: Any, T> put(model: M, options: Array<out Options.Write>, block: (Metadata, Body) -> T): T {
        val metadata = mdb.getMetadata(model, options)
        val typeName = mdb.typeTable.getTypeName(model::class)
        val body = Body {
            it.putShort(typeName.length.toShort())
            it.putAscii(typeName)
            mdb.serializer.serialize(model, it, *options)
        }
        return block(metadata, body)
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
        return put(model, options) { metadata, body ->
            val (key, size) = data.putAndGetNativeKey(mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class), metadata.primaryKey, body, metadata.indexes, *options)
            Sized(Key.Native(model::class, key), size)
        }
    }

    override fun delete(key: Key<*>, vararg options: Options.Write) {
        data.delete(key.bytes, *options)
    }
}
