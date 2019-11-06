package org.kodein.db.impl.model

import org.kodein.db.DBListener
import org.kodein.db.Options
import org.kodein.db.TypeTable
import org.kodein.db.data.DataDB
import org.kodein.db.impl.utils.newRWLock
import org.kodein.db.impl.utils.read
import org.kodein.db.impl.utils.write
import org.kodein.db.model.*
import org.kodein.db.model.orm.HasMetadata
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.Serializer
import org.kodein.memory.Closeable
import org.kodein.memory.io.ReadBuffer
import org.kodein.memory.io.Writeable
import org.kodein.memory.util.forEachResilient
import kotlin.reflect.KClass

internal class ModelDBImpl(private val defaultSerializer: Serializer<Any>?, userClassSerializers: Map<KClass<*>, Serializer<*>>, private val metadataExtractor: MetadataExtractor, val typeTable: TypeTable, override val data: DataDB) : ModelDB, ModelReadModule, ModelWriteModule, Closeable by data {

    private val listenersLock = newRWLock()
    private val listeners = LinkedHashSet<DBListener<Any>>()

    private val classSerializers = userClassSerializers + mapOf(
            IntPrimitive::class to IntPrimitive.S,
            LongPrimitive::class to LongPrimitive.S,
            DoublePrimitive::class to DoublePrimitive.S
    )

    @Suppress("UNCHECKED_CAST")
    internal fun serialize(model: Any, output: Writeable, vararg options: Options.Write) =
            (classSerializers[model::class] as? Serializer<Any>)?.serialize(model, output, *options)
                    ?: defaultSerializer?.serialize(model, output, *options)
                    ?: throw IllegalArgumentException("No serializer found for type ${model::class}")

    @Suppress("UNCHECKED_CAST")
    internal fun deserialize(type: KClass<out Any>, transientId: ReadBuffer, input: ReadBuffer, vararg options: Options.Read): Any =
            (classSerializers[type] as? Serializer<Any>)?.deserialize(type, transientId, input, *options)
                    ?: defaultSerializer?.deserialize(type, transientId, input, *options)
                    ?: throw IllegalArgumentException("No serializer found for type $type")

    internal fun getListeners() = listenersLock.read { listeners.toList() }
    internal fun <T> writeOnListeners(action: MutableSet<DBListener<Any>>.() -> T) = listenersLock.write { listeners.action() }

    override fun willAction(action: DBListener<Any>.() -> Unit) = getListeners().forEach(action)

    override fun didAction(action: DBListener<Any>.() -> Unit) = getListeners().forEachResilient(action)

    internal fun getMetadata(model: Any, options: Array<out Options.Write>) =
            (model as? HasMetadata)?.getMetadata(this, *options) ?: metadataExtractor.extractMetadata(model, *options)

    override val mdb: ModelDBImpl get() = this

    override fun newBatch(): ModelBatch = ModelBatchImpl(this, data.newBatch())

    override fun newSnapshot(vararg options: Options.Read): ModelSnapshot = ModelSnapshotImpl(this, data.newSnapshot())

    override fun register(listener: DBListener<Any>): Closeable {
        val subscription = Closeable { writeOnListeners { remove(listener) } }
        if (writeOnListeners { add(listener) }) listener.setSubscription(subscription)
        return subscription
    }

}
