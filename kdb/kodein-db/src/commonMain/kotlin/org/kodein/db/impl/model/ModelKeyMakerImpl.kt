package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataKeyMaker
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.native
import kotlin.reflect.KClass

internal interface ModelKeyMakerImpl : KeyMaker {

    val mdb: ModelDBImpl
    val data: DataKeyMaker

    override fun <M : Any> getHeapKey(type: KClass<M>, primaryKey: Value) = Key.Heap(type, data.getHeapKey(mdb.typeTable.getTypeName(type), primaryKey))
    override fun <M : Any> getNativeKey(type: KClass<M>, primaryKey: Value) = Key.Native(type, data.getNativeKey(mdb.typeTable.getTypeName(type), primaryKey))

    override fun <M : Any> getHeapKey(model: M, vararg options: Options.Write) = Key.Heap(model::class, data.getHeapKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).primaryKey))
    override fun <M : Any> getNativeKey(model: M, vararg options: Options.Write) = Key.Native(model::class, data.getNativeKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).primaryKey))

    override fun <M : Any> getRef(key: Key<M>): Ref<M> = mdb.serializer.getRef(key.bytes)

    override fun <M : Any> getHeapKey(type: KClass<M>, ref: Ref<M>): Key<M> = Key.Heap(type, mdb.serializer.getBytes(ref))
    override fun <M : Any> getNativeKey(type: KClass<M>, ref: Ref<M>): Key.Native<M> = Key.Native(type, mdb.serializer.getBytes(ref).let { Allocation.native(it.remaining) { putBytes(it) } })
}
