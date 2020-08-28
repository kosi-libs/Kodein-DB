package org.kodein.db.impl.model

import org.kodein.db.DBListener
import org.kodein.db.Options
import org.kodein.db.Sized
import org.kodein.db.TypeTable
import org.kodein.db.ascii.getAscii
import org.kodein.db.data.DataDB
import org.kodein.db.impl.utils.*
import org.kodein.db.model.*
import org.kodein.db.model.orm.HasMetadata
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.Serializer
import org.kodein.memory.Closeable
import org.kodein.memory.io.*
import org.kodein.memory.use
import org.kodein.memory.util.forEachResilient
import kotlin.reflect.KClass

internal class ModelDBImpl(private val defaultSerializer: Serializer<Any>?, userClassSerializers: Map<KClass<*>, Serializer<*>>, private val metadataExtractors: List<MetadataExtractor>, val typeTable: TypeTable, override val data: DataDB) : ModelDB, ModelReadModule, ModelWriteModule, Closeable by data {

    private val listenersLock = newRWLock()
    private val listeners = LinkedHashSet<DBListener<Any>>()

    internal val typeLock = newLock()
    private var nextTypeId: Int? = null
    private val typeNameMap = HashMap<ReadMemory, Int>()
    private val typeIdMap = HashMap<Int, ReadMemory>()

    private val typeCache = HashMap<Int, KClass<*>>()

    private val classSerializers = HashMap<KClass<*>, Serializer<*>>().apply {
        putAll(userClassSerializers)
        put(IntPrimitive::class, IntPrimitive.S)
        put(LongPrimitive::class, LongPrimitive.S)
        put(DoublePrimitive::class, DoublePrimitive.S)
        put(StringPrimitive::class, StringPrimitive.S)
        put(BytesPrimitive::class, BytesPrimitive.S)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun serialize(model: Any, output: Writeable, vararg options: Options.Write) =
            (classSerializers[model::class] as? Serializer<Any>)?.serialize(model, output, *options)
                    ?: defaultSerializer?.serialize(model, output, *options)
                    ?: throw IllegalArgumentException("No serializer found for type ${model::class}")

    internal fun getListeners() = listenersLock.read { listeners.toList() }

    private fun <T> writeOnListeners(action: MutableSet<DBListener<Any>>.() -> T) = listenersLock.write { listeners.action() }

    override fun willAction(action: DBListener<Any>.() -> Unit) = getListeners().forEach(action)

    override fun didAction(action: DBListener<Any>.() -> Unit) = getListeners().forEachResilient(action)

    internal fun getMetadata(model: Any, options: Array<out Options.Write>): Metadata {
        (model as? HasMetadata)?.getMetadata(this, *options)?.let { return it }

        metadataExtractors.forEach {
            it.extractMetadata(model, *options)?.let { return it }
        }

        error("Models does not implement neither HasMetadata nor Metadata, and no MetadataExtractor could extract metadata for $model")
    }

    override val mdb: ModelDBImpl get() = this

    override fun newBatch(): ModelBatch = ModelBatchImpl(this, data.newBatch())

    override fun newSnapshot(vararg options: Options.Read): ModelSnapshot = ModelSnapshotImpl(this, data.newSnapshot())

    override fun register(listener: DBListener<Any>): Closeable {
        val subscription = Closeable { writeOnListeners { remove(listener) } }
        if (writeOnListeners { add(listener) }) listener.setSubscription(subscription)
        return subscription
    }

    internal fun getTypeId(typeName: ReadMemory, createIfNone: Boolean = true): Int =
            typeNameMap[typeName] ?: typeLock.withLock {
                typeNameMap[typeName] ?: Allocation.native(getTypeNameKeySize(typeName)) { putTypeNameKey(typeName) }.use { typeNameKey ->
                    data.ldb.get(typeNameKey)?.use {
                        it.readInt().also { typeId ->
                            typeNameMap[typeName] = typeId
                            typeIdMap[typeId] = typeName
                        }
                    } ?: run {
                        if (!createIfNone) return 0
                        val typeId = nextTypeId ?: data.ldb.get(nextTypeKey)?.use { it.readInt() } ?.also { nextTypeId = it } ?: 1
                        check(typeId != 0) { "No more type int available. Have you inserted UINT_MAX different types in this database ?!?!?!" }
                        Allocation.native(typeIdKeySize) { putTypeIdKey(typeId) }.use { typeIdKey ->
                            data.ldb.newWriteBatch().use {
                                it.put(typeNameKey, KBuffer.array(4) { putInt(typeId) })
                                it.put(typeIdKey, typeName)
                                it.put(nextTypeKey, KBuffer.array(4) { putInt(typeId + 1) })
                                data.ldb.write(it)
                            }
                            typeNameMap[typeName] = typeId
                            typeIdMap[typeId] = typeName
                            nextTypeId = typeId + 1
                            typeId
                        }
                    }
                }
            }

    internal fun getTypeName(typeId: Int): ReadMemory? =
            typeIdMap[typeId] ?: typeLock.withLock {
                typeIdMap[typeId] ?: Allocation.native(typeIdKeySize) { putTypeIdKey(typeId) }.use { typeIdKey ->
                    data.ldb.get(typeIdKey)?.use { alloc ->
                        KBuffer.arrayCopy(alloc).also { typeName ->
                            typeNameMap[typeName] = typeId
                            typeIdMap[typeId] = typeName
                        }
                    }
                }
            }

    internal fun <M : Any> deserialize(type: KClass<out M>, transientId: ReadMemory, body: ReadMemory, options: Array<out Options.Read>): Sized<M> {
        body.markBuffer { buffer ->
            val typeId = buffer.readInt()
            val realType = typeCache[typeId] ?: run {
                val typeName = getTypeName(typeId) ?: throw IllegalStateException("Unknown type ID. Has this LevelDB entry been inserted outside of Kodein DB?")
                typeTable.getTypeClass(typeName) ?: run {
                    check(type != Any::class) { "Type ${typeName.getAscii()} is not declared in type table." }
                    val expectedTypeName = typeTable.getTypeName(type)
                    check(typeName.compareTo(expectedTypeName) == 0) { "Type ${typeName.getAscii()} is not declared in type table and do not match expected type ${expectedTypeName.getAscii()}." }
                    type
                }.also { typeCache[typeId] = it }
            }

            @Suppress("UNCHECKED_CAST")
            realType as KClass<M>

            val r = buffer.available

            @Suppress("UNCHECKED_CAST")
            val model = ((classSerializers[realType] as? Serializer<Any>)?.deserialize(realType, transientId, buffer, *options)
                    ?: defaultSerializer?.deserialize(realType, transientId, buffer, *options)
                    ?: throw IllegalArgumentException("No serializer found for type $realType")) as M

            return Sized(model, r - buffer.available)
        }
    }

}
