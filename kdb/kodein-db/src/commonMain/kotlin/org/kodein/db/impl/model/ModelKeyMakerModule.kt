package org.kodein.db.impl.model

import org.kodein.db.*
import org.kodein.db.data.DataKeyMaker
import kotlin.reflect.KClass

internal interface ModelKeyMakerModule : KeyMaker {

    val mdb: ModelDBImpl
    val data: DataKeyMaker

    override fun <M : Any> newHeapKey(type: KClass<M>, id: Value) = Key.Heap<M>(data.newHeapKey(mdb.typeTable.getTypeName(type), id))
    override fun <M : Any> newNativeKey(type: KClass<M>, id: Value) = Key.Native<M>(data.newNativeKey(mdb.typeTable.getTypeName(type), id))

    override fun <M : Any> newHeapKey(model: M, vararg options: Options.Write) = Key.Heap<M>(data.newHeapKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).id))
    override fun <M : Any> newNativeKey(model: M, vararg options: Options.Write) = Key.Native<M>(data.newNativeKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).id))
}
