package org.kodein.db.impl.model

import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.data.DataBase
import org.kodein.db.model.Key
import org.kodein.db.model.ModelBase
import kotlin.reflect.KClass

internal interface BaseModelBase : ModelBase {

    val mdb: ModelDBImpl
    val data: DataBase

    override fun <M : Any> getHeapKey(type: KClass<M>, primaryKey: Value) = Key.Heap(type, data.getHeapKey(mdb.typeTable.getTypeName(type), primaryKey))
    override fun <M : Any> getNativeKey(type: KClass<M>, primaryKey: Value) = Key.Native(type, data.getNativeKey(mdb.typeTable.getTypeName(type), primaryKey))

    override fun <M : Any> getHeapKey(model: M, vararg options: Options.Write) = Key.Heap(model::class, data.getHeapKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).primaryKey))
    override fun <M : Any> getNativeKey(model: M, vararg options: Options.Write) = Key.Native(model::class, data.getNativeKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).primaryKey))

}
