package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataKeyMaker
import org.kodein.memory.io.Allocation
import org.kodein.memory.io.native
import kotlin.reflect.KClass

internal interface ModelKeyMakerImpl : KeyMaker {

    val mdb: ModelDBImpl
    val data: DataKeyMaker

    override fun <M : Any> getHeapKey(type: KClass<M>, primaryKey: Value) = Key.Heap<M>(data.getHeapKey(mdb.typeTable.getTypeName(type), primaryKey))
    override fun <M : Any> getNativeKey(type: KClass<M>, primaryKey: Value) = Key.Native<M>(data.getNativeKey(mdb.typeTable.getTypeName(type), primaryKey))

    override fun <M : Any> getHeapKey(model: M, vararg options: Options.Write) = Key.Heap<M>(data.getHeapKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).primaryKey))
    override fun <M : Any> getNativeKey(model: M, vararg options: Options.Write) = Key.Native<M>(data.getNativeKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).primaryKey))
}
