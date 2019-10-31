package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.ascii.putAscii
import org.kodein.db.ascii.readAscii
import org.kodein.db.data.DataWrite
import org.kodein.db.impl.data.getObjectKeyType
import org.kodein.db.impl.data.putObjectKey
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.ModelWrite
import org.kodein.memory.Closeable
import org.kodein.memory.io.mark
import org.kodein.memory.io.verify
import org.kodein.memory.use
import kotlin.reflect.KClass

internal interface ModelWriteModule : ModelKeyMakerModule, ModelWrite {

    override val data: DataWrite

    fun willAction(action: DBListener<Any>.() -> Unit)

    fun didAction(action: DBListener<Any>.() -> Unit)

    fun handleCloseable(closeable: Closeable): Closeable

    private fun <M: Any> put(model: M, options: Array<out Options.Write>, block: (String, Metadata) -> Pair<Key<*>, Closeable>): Int {
        val metadata = mdb.getMetadata(model, options)
        val typeName = mdb.typeTable.getTypeName(model::class)
        val rootTypeName = mdb.typeTable.getTypeName(mdb.typeTable.getRootOf(model::class) ?: model::class)
        willAction { willPut(model, rootTypeName, metadata, options) }
        val body = Body {
            it.putShort(typeName.length.toShort())
            it.putAscii(typeName)
            mdb.serializer.serialize(model, it, *options)
        }
        val (key, closeable) = block(rootTypeName, metadata)
        closeable.use {
            val size = data.put(key.bytes, body, metadata.indexes, *options)
            didAction { didPut(model, key, rootTypeName, metadata, size, options) }
            return size
        }
    }

    override fun put(model: Any, vararg options: Options.Write): Int =
        put(model, options) { rootTypeName, metadata ->
            val key = Key.Native<Any>(data.newNativeKey(rootTypeName, metadata.id))
            Pair(key, handleCloseable(key))
        }

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.Write): Int =
        put(model, options) { rootTypeName, metadata ->
            mark(key.bytes) {
                verify(key.bytes) { putObjectKey(rootTypeName, metadata.id) }
            }
            key to Closeable {}
        }

    override fun <M: Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.Write) {
        val typeName = getObjectKeyType(key.bytes).readAscii()
        var fetched = false
        var model: Any? = null
        val getModel: () -> Any? = {
            if (!fetched) {
                model = mdb[type, key]?.value
                fetched = true
            }
            model
        }
        willAction { willDelete(key, getModel, typeName, options) }
        data.delete(key.bytes, *options)
        didAction { didDelete(key, model, typeName, options) }
    }
}
