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

    override fun <M : Any> getKey(type: KClass<M>, primaryKey: Value) = Key(type, data.getKey(mdb.typeTable.getTypeName(type), primaryKey))

    override fun <M : Any> getKey(model: M, vararg options: Options.Write): Key<M> = Key(model::class, data.getKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).primaryKey))

}
