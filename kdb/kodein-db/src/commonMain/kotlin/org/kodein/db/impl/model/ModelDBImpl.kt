package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataDB
import org.kodein.db.impl.utils.*
import org.kodein.db.model.*
import org.kodein.db.model.orm.HasMetadata
import org.kodein.db.model.orm.Metadata
import org.kodein.db.model.orm.MetadataExtractor
import org.kodein.db.model.orm.Serializer
import org.kodein.memory.Closeable
import org.kodein.memory.io.*
import org.kodein.memory.text.readString
import org.kodein.memory.use
import org.kodein.memory.util.deferScope
import org.kodein.memory.util.forEachResilient
import kotlin.reflect.KClass

internal class ModelDBImpl(
    private val defaultSerializer: Serializer<Any>?,
    userClassSerializers: Map<KClass<*>, Serializer<*>>,
    private val metadataExtractors: List<MetadataExtractor>,
    internal val valueConverters: List<ValueConverter>,
    override val typeTable: TypeTable,
    override val data: DataDB
) : ModelDB, ModelReadModule, ModelWriteModule<DataDB>, Closeable by data {

    private val listenersLock = newRWLock()
    private val listeners = LinkedHashSet<DBListener<Any>>()

    private val typeLock = newLock()
    private var nextTypeId: Int? = null
    private val typeNameMap = HashMap<ReadMemory, Int>()
    private val typeIdMap = HashMap<Int, ReadMemory>()

    private val typeCache = HashMap<Int, KClass<*>>()

    @OptIn(ExperimentalStdlibApi::class)
    private val classSerializers = buildMap<KClass<*>, Serializer<*>> {
        putAll(userClassSerializers)

        @Suppress("DEPRECATION_ERROR")
        put(IntPrimitive::class, IntPrimitive.S)
        @Suppress("DEPRECATION_ERROR")
        put(LongPrimitive::class, LongPrimitive.S)
        @Suppress("DEPRECATION_ERROR")
        put(DoublePrimitive::class, DoublePrimitive.S)
        @Suppress("DEPRECATION_ERROR")
        put(StringPrimitive::class, StringPrimitive.S)
        @Suppress("DEPRECATION_ERROR")
        put(BytesPrimitive::class, BytesPrimitive.S)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun serialize(model: Any, output: Writeable, vararg options: Options.Puts) =
            (classSerializers[model::class] as? Serializer<Any>)?.serialize(model, output, *options)
                    ?: defaultSerializer?.serialize(model, output, *options)
                    ?: throw IllegalArgumentException("No serializer found for type ${model::class}")

    internal fun getListeners() = listenersLock.readLock().withLock { listeners.toList() }

    private fun <T> writeOnListeners(action: MutableSet<DBListener<Any>>.() -> T) = listenersLock.writeLock().withLock { listeners.action() }

    override fun willAction(action: DBListener<Any>.() -> Unit) = getListeners().forEach(action)

    override fun didAction(action: DBListener<Any>.() -> Unit) = getListeners().forEachResilient(action)

    internal fun getMetadata(model: Any, options: Array<out Options.Puts>): Metadata {
        (model as? HasMetadata)?.getMetadata(this, *options)?.let { return it }

        metadataExtractors.forEach {
            it.extractMetadata(model, *options)?.let { return it }
        }

        error("Models does not implement neither HasMetadata nor Metadata, and no MetadataExtractor could extract metadata for $model")
    }

    override val mdb: ModelDBImpl get() = this

    override fun <M : Any> put(model: M, vararg options: Options.DirectPut): KeyAndSize<M> =
        put(model, options, DataDB::put)

    override fun <M : Any> put(key: Key<M>, model: M, vararg options: Options.DirectPut): Int =
        put(key, model, options, DataDB::put)

    override fun <M : Any> delete(type: KClass<M>, key: Key<M>, vararg options: Options.DirectDelete) {
        delete(type, key, options, DataDB::delete)
    }

    override fun newBatch(vararg options: Options.NewBatch): ModelBatch = ModelBatchImpl(this, data.newBatch(*options))

    override fun newSnapshot(vararg options: Options.NewSnapshot): ModelSnapshot = ModelSnapshotImpl(this, data.newSnapshot(*options))

    override fun register(listener: DBListener<Any>): Closeable {
        val subscription = Closeable { writeOnListeners { remove(listener) } }
        if (writeOnListeners { add(listener) }) listener.setSubscription(subscription)
        return subscription
    }

    override fun getTypeId(typeName: ReadMemory, createIfNone: Boolean): Int {
        typeNameMap[typeName]?.let { return it }

        deferScope {
            lockInScope(typeLock)

            typeNameMap[typeName]?.let { return it }

            val typeNameKey = Allocation.native(getTypeNameKeySize(typeName)) { putTypeNameKey(typeName) }.useInScope()

            val existingTypeId = data.kv.get(typeNameKey)?.use { it.getInt(0) }
            if (existingTypeId != null) {
                typeNameMap[typeName] = existingTypeId
                typeIdMap[existingTypeId] = typeName
                return existingTypeId
            }

            if (!createIfNone) return 0

            val newTypeId = nextTypeId ?: data.kv.get(nextTypeKey)?.use { it.getInt(0) } ?.also { nextTypeId = it } ?: 1
            check(newTypeId != 0) { "No more type int available. Have you inserted UINT_MAX different types in this database ?!?!?!" }
            val typeIdKey = Allocation.native(typeIdKeySize) { putTypeIdKey(newTypeId) } .useInScope()
            data.kv.newBatch().use {
                it.put(typeNameKey, Memory.array(4) { writeInt(newTypeId) })
                it.put(typeIdKey, typeName)
                it.put(nextTypeKey, Memory.array(4) { writeInt(newTypeId + 1) })
                it.write()
            }
            typeNameMap[typeName] = newTypeId
            typeIdMap[newTypeId] = typeName
            nextTypeId = newTypeId + 1
            return newTypeId
        }
    }

    override fun getTypeName(typeId: Int): ReadMemory? {
        typeIdMap[typeId]?.let { return it }
        deferScope {
            lockInScope(typeLock)
            val typeIdKey = Allocation.native(typeIdKeySize) { putTypeIdKey(typeId) } .useInScope()
            return data.kv.get(typeIdKey)?.use { alloc ->
                Memory.arrayCopy(alloc).also { typeName ->
                    typeNameMap[typeName] = typeId
                    typeIdMap[typeId] = typeName
                }
            }
        }
    }

    internal fun <M : Any> deserialize(type: KClass<out M>, transientId: ReadMemory, body: ReadMemory, options: Array<out Options.Get>): Sized<M> {
        val r = body.asReadable()

        val typeId = r.readInt()
        val realType = typeCache[typeId] ?: run {
            val typeName = getTypeName(typeId) ?: throw IllegalStateException("Unknown type ID. Has this LevelDB entry been inserted outside of Kodein DB?")
            typeTable.getTypeClass(typeName) ?: run {
                check(type != Any::class) { "Type ${typeName.readString()} is not declared in type table." }
                val expectedTypeName = typeTable.getTypeName(type)
                check(typeName.compareTo(expectedTypeName) == 0) { "Type ${typeName.readString()} is not declared in type table and do not match expected type ${expectedTypeName.readString()}." }
                type
            }.also { typeCache[typeId] = it }
        }

        @Suppress("UNCHECKED_CAST")
        realType as KClass<M>

        val size = r.remaining

        @Suppress("UNCHECKED_CAST", "DEPRECATION_ERROR")
        val model = ((classSerializers[realType] as? Serializer<Any>)?.deserialize(realType, transientId, r, *options)
            ?: defaultSerializer?.deserialize(realType, transientId, r, *options)
            ?: throw IllegalArgumentException("No serializer found for type $realType")) as M

        check (r.remaining == 0) { "Deserializer has not consumed the entire body (left ${r.remaining} bytes)" }

        return Sized(model, size)
    }

    override fun <T : Any> getExtension(key: ExtensionKey<T>): T? = data.getExtension(key)

}
