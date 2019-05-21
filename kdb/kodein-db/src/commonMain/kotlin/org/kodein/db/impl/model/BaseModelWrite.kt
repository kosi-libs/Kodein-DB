package org.kodein.db.impl.model

import org.kodein.db.Body
import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.ascii.putAscii
import org.kodein.db.data.DataRead
import org.kodein.db.data.DataWrite
import org.kodein.db.model.*

internal interface BaseModelWrite : BaseModelBase, ModelWrite {

    override val data: DataWrite

    override fun put(model: Any, vararg options: Options.Write) {
        val metadata = mdb.getMetadata(model, options)
        val typeName = mdb.typeTable.getTypeName(model::class)
        val body = Body {
            it.putShort(typeName.length.toShort())
            it.putAscii(typeName)
            mdb.serializer.serialize(model, it, *options)
        }
        data.put(mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class), metadata.primaryKey, body, metadata.indexes, *options)
    }

    override fun <M : Any> putAndGetKey(model: M, vararg options: Options.Write): Key<M> {
        val metadata = mdb.getMetadata(model, options)
        val typeName = mdb.typeTable.getTypeName(model::class)
        val body = Body {
            it.putShort(typeName.length.toShort())
            it.putAscii(typeName)
            mdb.serializer.serialize(model, it, *options)
        }
        val result = data.putAndGetKey(mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class), metadata.primaryKey, body, metadata.indexes, *options)
        return Key(model::class, result.key)
    }

    override fun delete(key: Key<*>, vararg options: Options.Write) {
        data.delete(key.bytes, *options)
    }
}
