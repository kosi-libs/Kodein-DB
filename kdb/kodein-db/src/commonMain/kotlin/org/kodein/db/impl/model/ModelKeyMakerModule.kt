package org.kodein.db.impl.model

import org.kodein.db.Key
import org.kodein.db.Options
import org.kodein.db.Value
import org.kodein.db.data.DataKeyMaker
import org.kodein.db.model.ModelKeyMaker
import kotlin.reflect.KClass

internal interface ModelKeyMakerModule : ModelKeyMaker {

    val mdb: ModelDBImpl
    val data: DataKeyMaker

    override fun <M : Any> newKey(type: KClass<M>, id: Value) = Key<M>(data.newKey(mdb.typeTable.getTypeName(type), id))

    override fun <M : Any> newKeyFrom(model: M, vararg options: Options.Write) = Key<M>(data.newKey(mdb.typeTable.getTypeName(model::class), mdb.getMetadata(model, options).id))
}
